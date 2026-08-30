// Package store defines the persistence boundary for the EasyLAN web
// platform.  All database/Redis-backed implementations live in this
// package so the rest of the application talks to a single interface.
package store

import (
	"context"
	"errors"
	"time"

	"github.com/xiaoxianhw/easylan/web/internal/room"
)

// ErrNotFound is returned when a record cannot be located.
var ErrNotFound = errors.New("store: not found")

// ErrConflict is returned when a uniqueness constraint is violated, e.g.
// when two callers race to create a room with the same code.
var ErrConflict = errors.New("store: conflict")

// RoomFilter narrows queries against the room collection.
type RoomFilter struct {
	Loader   string // optional: forge / fabric / neoforge / vanilla
	Version  string // optional: exact MC version
	Region   string // optional: substring match on region
	Search   string // optional: substring match on name / motd
	OnlyOpen bool   // if true exclude rooms currently full
	Limit    int    // pagination
	Offset   int
}

// Store is the persistence interface used by the API layer.
type Store interface {
	// Rooms.
	CreateRoom(ctx context.Context, r *room.Room) error
	GetRoomByCode(ctx context.Context, code string) (*room.Room, error)
	UpdateRoom(ctx context.Context, r *room.Room) error
	DeleteRoom(ctx context.Context, code string) error
	ListRooms(ctx context.Context, f RoomFilter) ([]*room.Room, int, error)
	TouchRoom(ctx context.Context, code string, expiresAt time.Time) error

	// Maintenance.
	PruneExpiredRooms(ctx context.Context, now time.Time) (int, error)

	// Lifecycle.
	Close() error
}

// Cache is an optional Redis-style fast cache used for ephemeral state
// (NAT-detection lookups, room TTL pings, signaling envelopes).
//
// Implementations may be nil — the API layer treats Cache as best-effort
// and degrades gracefully when nothing is configured.
type Cache interface {
	Set(ctx context.Context, key string, value []byte, ttl time.Duration) error
	Get(ctx context.Context, key string) ([]byte, error)
	Delete(ctx context.Context, key string) error
	Close() error
}
