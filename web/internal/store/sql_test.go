package store

import (
	"context"
	"database/sql"
	"testing"
	"time"

	_ "modernc.org/sqlite"

	"github.com/xiaoxianhw/easylan/web/internal/room"
)

func openTestSQLite(t *testing.T) *SQLStore {
	t.Helper()
	db, err := sql.Open("sqlite", ":memory:")
	if err != nil {
		t.Fatalf("sql.Open: %v", err)
	}
	db.SetMaxOpenConns(1)
	st, err := NewSQLStore(db, DialectSQLite)
	if err != nil {
		t.Fatalf("NewSQLStore: %v", err)
	}
	t.Cleanup(func() { _ = st.Close() })
	return st
}

func sampleRoom() *room.Room {
	return &room.Room{
		Code:       "ABCDE-FGHIJ",
		Name:       "Test Room",
		OwnerToken: "owner-token",
		OwnerName:  "Devin",
		Loader:     room.LoaderForge,
		Version:    "1.20.1",
		Modes:      []room.Mode{room.ModeP2P, room.ModeIPv6},
		Motd:       "Hello, MC!",
		MaxPlayers: 4,
		HostPort:   25565,
		IsPublic:   true,
		Tags:       []string{"vanilla"},
		ExpiresAt:  time.Now().Add(15 * time.Minute).UTC(),
	}
}

func TestSQLStoreCRUD(t *testing.T) {
	st := openTestSQLite(t)
	ctx := context.Background()
	r := sampleRoom()
	if err := st.CreateRoom(ctx, r); err != nil {
		t.Fatalf("CreateRoom: %v", err)
	}
	got, err := st.GetRoomByCode(ctx, r.Code)
	if err != nil {
		t.Fatalf("GetRoomByCode: %v", err)
	}
	if got.Name != r.Name || got.Loader != r.Loader || got.Version != r.Version {
		t.Errorf("GetRoomByCode mismatch: %+v vs %+v", got, r)
	}
	if len(got.Modes) != 2 || got.Modes[0] != room.ModeP2P {
		t.Errorf("modes round-trip failed: %v", got.Modes)
	}

	got.Motd = "updated"
	got.OnlinePly = 2
	got.Players = []string{"alice", "bob"}
	if err := st.UpdateRoom(ctx, got); err != nil {
		t.Fatalf("UpdateRoom: %v", err)
	}
	got2, err := st.GetRoomByCode(ctx, r.Code)
	if err != nil {
		t.Fatalf("GetRoomByCode after update: %v", err)
	}
	if got2.Motd != "updated" || got2.OnlinePly != 2 || len(got2.Players) != 2 {
		t.Errorf("UpdateRoom did not persist: %+v", got2)
	}

	items, total, err := st.ListRooms(ctx, RoomFilter{Loader: "forge", Limit: 10})
	if err != nil {
		t.Fatalf("ListRooms: %v", err)
	}
	if total != 1 || len(items) != 1 {
		t.Errorf("ListRooms expected 1 result, got total=%d items=%d", total, len(items))
	}

	if err := st.DeleteRoom(ctx, r.Code); err != nil {
		t.Fatalf("DeleteRoom: %v", err)
	}
	if _, err := st.GetRoomByCode(ctx, r.Code); err == nil {
		t.Errorf("expected ErrNotFound after delete")
	}
}

func TestSQLStorePruneExpired(t *testing.T) {
	st := openTestSQLite(t)
	ctx := context.Background()
	now := time.Now().UTC()
	a := sampleRoom()
	a.Code = "AAAAA-AAAAA"
	a.ExpiresAt = now.Add(-time.Minute)
	b := sampleRoom()
	b.Code = "BBBBB-BBBBB"
	b.ExpiresAt = now.Add(time.Hour)
	if err := st.CreateRoom(ctx, a); err != nil {
		t.Fatal(err)
	}
	if err := st.CreateRoom(ctx, b); err != nil {
		t.Fatal(err)
	}
	n, err := st.PruneExpiredRooms(ctx, now)
	if err != nil {
		t.Fatalf("PruneExpiredRooms: %v", err)
	}
	if n != 1 {
		t.Errorf("expected 1 pruned, got %d", n)
	}
	if _, err := st.GetRoomByCode(ctx, "BBBBB-BBBBB"); err != nil {
		t.Errorf("expected non-expired room to survive: %v", err)
	}
}
