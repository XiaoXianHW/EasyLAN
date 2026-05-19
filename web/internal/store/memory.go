package store

import (
	"context"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/xiaoxianhw/easylan/web/internal/room"
)

// MemoryStore is a process-local Store backed by a plain map.  It is the
// zero-config option used by tests and `--storage memory` deployments.
type MemoryStore struct {
	mu    sync.RWMutex
	rooms map[string]*room.Room
	id    int64
}

// NewMemoryStore returns an empty in-memory Store.
func NewMemoryStore() *MemoryStore {
	return &MemoryStore{rooms: make(map[string]*room.Room)}
}

// CreateRoom stores r and returns ErrConflict if r.Code is already taken.
func (m *MemoryStore) CreateRoom(_ context.Context, r *room.Room) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if _, ok := m.rooms[r.Code]; ok {
		return ErrConflict
	}
	m.id++
	r.ID = m.id
	if r.CreatedAt.IsZero() {
		r.CreatedAt = time.Now().UTC()
	}
	r.UpdatedAt = r.CreatedAt
	clone := *r
	m.rooms[r.Code] = &clone
	return nil
}

func (m *MemoryStore) GetRoomByCode(_ context.Context, code string) (*room.Room, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()
	r, ok := m.rooms[code]
	if !ok {
		return nil, ErrNotFound
	}
	clone := *r
	return &clone, nil
}

func (m *MemoryStore) UpdateRoom(_ context.Context, r *room.Room) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if _, ok := m.rooms[r.Code]; !ok {
		return ErrNotFound
	}
	r.UpdatedAt = time.Now().UTC()
	clone := *r
	m.rooms[r.Code] = &clone
	return nil
}

func (m *MemoryStore) DeleteRoom(_ context.Context, code string) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if _, ok := m.rooms[code]; !ok {
		return ErrNotFound
	}
	delete(m.rooms, code)
	return nil
}

func (m *MemoryStore) ListRooms(_ context.Context, f RoomFilter) ([]*room.Room, int, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()
	out := make([]*room.Room, 0, len(m.rooms))
	for _, r := range m.rooms {
		if !matchRoom(r, f) {
			continue
		}
		clone := *r
		out = append(out, &clone)
	}
	sort.Slice(out, func(i, j int) bool {
		return out[i].UpdatedAt.After(out[j].UpdatedAt)
	})
	total := len(out)
	if f.Offset > 0 {
		if f.Offset >= len(out) {
			out = nil
		} else {
			out = out[f.Offset:]
		}
	}
	if f.Limit > 0 && len(out) > f.Limit {
		out = out[:f.Limit]
	}
	return out, total, nil
}

func (m *MemoryStore) TouchRoom(_ context.Context, code string, expiresAt time.Time) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	r, ok := m.rooms[code]
	if !ok {
		return ErrNotFound
	}
	r.ExpiresAt = expiresAt
	r.UpdatedAt = time.Now().UTC()
	return nil
}

func (m *MemoryStore) PruneExpiredRooms(_ context.Context, now time.Time) (int, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	removed := 0
	for code, r := range m.rooms {
		if !r.ExpiresAt.IsZero() && r.ExpiresAt.Before(now) {
			delete(m.rooms, code)
			removed++
		}
	}
	return removed, nil
}

// Close is a no-op so MemoryStore satisfies the Store interface.
func (m *MemoryStore) Close() error { return nil }

func matchRoom(r *room.Room, f RoomFilter) bool {
	if f.Loader != "" && string(r.Loader) != f.Loader {
		return false
	}
	if f.Version != "" && r.Version != f.Version {
		return false
	}
	if f.Region != "" && !strings.Contains(strings.ToLower(r.Region), strings.ToLower(f.Region)) {
		return false
	}
	if f.Search != "" {
		needle := strings.ToLower(f.Search)
		if !strings.Contains(strings.ToLower(r.Name), needle) &&
			!strings.Contains(strings.ToLower(r.Motd), needle) {
			return false
		}
	}
	if f.OnlyOpen && r.MaxPlayers > 0 && r.OnlinePly >= r.MaxPlayers {
		return false
	}
	return true
}

// MemoryCache is a process-local Cache.  Useful for `--cache memory`.
type MemoryCache struct {
	mu      sync.Mutex
	entries map[string]memoryCacheEntry
}

type memoryCacheEntry struct {
	value     []byte
	expiresAt time.Time
}

// NewMemoryCache returns an empty MemoryCache.
func NewMemoryCache() *MemoryCache {
	return &MemoryCache{entries: make(map[string]memoryCacheEntry)}
}

func (m *MemoryCache) Set(_ context.Context, key string, value []byte, ttl time.Duration) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	expires := time.Time{}
	if ttl > 0 {
		expires = time.Now().Add(ttl)
	}
	m.entries[key] = memoryCacheEntry{value: append([]byte(nil), value...), expiresAt: expires}
	return nil
}

func (m *MemoryCache) Get(_ context.Context, key string) ([]byte, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	e, ok := m.entries[key]
	if !ok {
		return nil, ErrNotFound
	}
	if !e.expiresAt.IsZero() && e.expiresAt.Before(time.Now()) {
		delete(m.entries, key)
		return nil, ErrNotFound
	}
	return append([]byte(nil), e.value...), nil
}

func (m *MemoryCache) Delete(_ context.Context, key string) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	delete(m.entries, key)
	return nil
}

// Close is a no-op so MemoryCache satisfies the Cache interface.
func (m *MemoryCache) Close() error { return nil }
