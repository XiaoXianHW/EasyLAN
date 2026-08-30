package store

import (
	"context"
	"errors"
	"time"

	"github.com/redis/go-redis/v9"
)

// RedisCache wraps a *redis.Client to satisfy Cache.
type RedisCache struct {
	client *redis.Client
}

// NewRedisCache returns a RedisCache backed by the given *redis.Client.
func NewRedisCache(client *redis.Client) *RedisCache {
	return &RedisCache{client: client}
}

// Set stores value at key with the given ttl (zero means "no expiry").
func (r *RedisCache) Set(ctx context.Context, key string, value []byte, ttl time.Duration) error {
	return r.client.Set(ctx, key, value, ttl).Err()
}

// Get fetches the value at key, returning ErrNotFound when missing.
func (r *RedisCache) Get(ctx context.Context, key string) ([]byte, error) {
	b, err := r.client.Get(ctx, key).Bytes()
	if errors.Is(err, redis.Nil) {
		return nil, ErrNotFound
	}
	return b, err
}

// Delete removes key, ignoring missing entries.
func (r *RedisCache) Delete(ctx context.Context, key string) error {
	return r.client.Del(ctx, key).Err()
}

// Close releases the underlying connection pool.
func (r *RedisCache) Close() error { return r.client.Close() }
