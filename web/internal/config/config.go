// Package config holds runtime configuration for the EasyLAN web platform.
package config

import (
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"
)

// Config is the resolved configuration loaded once at startup.
type Config struct {
	Listen        string
	StunListen    string // bind address, e.g. ":3478"
	StunPublic    string // public STUN URL advertised to clients (host:port)
	StorageDriver string // sqlite | mysql | memory
	StorageDSN    string
	CacheDriver   string // redis | memory | none
	CacheURL      string
	GeoIPPath     string
	PublicBaseURL string
	RoomTTL       time.Duration
	LogLevel      string
	Env           string
}

// FromEnv reads configuration from process environment variables.  Sane
// defaults are chosen so `./easylan-web` works out of the box with a
// local SQLite file and an in-memory cache.
func FromEnv() (*Config, error) {
	cfg := &Config{
		Listen:        envDefault("EASYLAN_LISTEN", ":8080"),
		StunListen:    envDefault("EASYLAN_STUN_LISTEN", ":3478"),
		StunPublic:    envDefault("EASYLAN_STUN_PUBLIC", ""),
		StorageDriver: strings.ToLower(envDefault("EASYLAN_STORAGE_DRIVER", "sqlite")),
		StorageDSN:    envDefault("EASYLAN_STORAGE_DSN", "easylan.db"),
		CacheDriver:   strings.ToLower(envDefault("EASYLAN_CACHE_DRIVER", "memory")),
		CacheURL:      envDefault("EASYLAN_CACHE_URL", ""),
		GeoIPPath:     envDefault("EASYLAN_GEOIP_PATH", ""),
		PublicBaseURL: envDefault("EASYLAN_PUBLIC_BASE_URL", ""),
		LogLevel:      strings.ToLower(envDefault("EASYLAN_LOG_LEVEL", "info")),
		Env:           strings.ToLower(envDefault("EASYLAN_ENV", "development")),
	}

	ttlSeconds, err := strconv.Atoi(envDefault("EASYLAN_ROOM_TTL_SECONDS", "900"))
	if err != nil {
		return nil, fmt.Errorf("config: invalid EASYLAN_ROOM_TTL_SECONDS: %w", err)
	}
	cfg.RoomTTL = time.Duration(ttlSeconds) * time.Second

	switch cfg.StorageDriver {
	case "sqlite", "mysql", "memory":
	default:
		return nil, fmt.Errorf("config: unsupported storage driver %q", cfg.StorageDriver)
	}
	switch cfg.CacheDriver {
	case "redis", "memory", "none":
	default:
		return nil, fmt.Errorf("config: unsupported cache driver %q", cfg.CacheDriver)
	}
	return cfg, nil
}

func envDefault(key, def string) string {
	if v, ok := os.LookupEnv(key); ok && strings.TrimSpace(v) != "" {
		return v
	}
	return def
}
