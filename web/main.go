// Command easylan-web boots the EasyLAN Minecraft LAN platform.  It
// serves the embedded Next.js frontend, exposes the JSON API used by
// the frontend and the in-game mod, and runs an internal STUN server.
package main

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"log/slog"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/redis/go-redis/v9"
	_ "github.com/go-sql-driver/mysql"
	_ "modernc.org/sqlite"

	"github.com/xiaoxianhw/easylan/web/internal/api"
	"github.com/xiaoxianhw/easylan/web/internal/config"
	"github.com/xiaoxianhw/easylan/web/internal/geoip"
	"github.com/xiaoxianhw/easylan/web/internal/server"
	"github.com/xiaoxianhw/easylan/web/internal/store"
	"github.com/xiaoxianhw/easylan/web/internal/stun"
)

func main() {
	if err := run(); err != nil {
		fmt.Fprintf(os.Stderr, "fatal: %v\n", err)
		os.Exit(1)
	}
}

func run() error {
	cfg, err := config.FromEnv()
	if err != nil {
		return err
	}

	logger := newLogger(cfg.LogLevel)
	logger.Info("config loaded",
		"listen", cfg.Listen,
		"stun", cfg.StunListen,
		"storage", cfg.StorageDriver,
		"cache", cfg.CacheDriver,
		"env", cfg.Env,
	)

	st, err := openStore(cfg, logger)
	if err != nil {
		return fmt.Errorf("storage: %w", err)
	}
	defer st.Close()

	cache, err := openCache(cfg)
	if err != nil {
		return fmt.Errorf("cache: %w", err)
	}
	if cache != nil {
		defer cache.Close()
	}

	geoResolver, err := geoip.NewResolver(cfg.GeoIPPath)
	if err != nil {
		return fmt.Errorf("geoip: %w", err)
	}
	defer geoResolver.Close()
	if !geoResolver.HasDatabase() {
		logger.Warn("geoip: no database configured (set EASYLAN_GEOIP_PATH to enable city-level lookups)")
	}

	rootCtx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer cancel()

	stunServer, err := stun.New(cfg.StunListen, logger)
	if err != nil {
		return fmt.Errorf("stun: %w", err)
	}
	go func() {
		if err := stunServer.Run(rootCtx); err != nil {
			logger.Error("stun: exit", "err", err)
		}
	}()

	go pruneLoop(rootCtx, logger, st)

	apiDeps := api.Deps{
		Logger:     logger,
		Store:      st,
		Cache:      cache,
		GeoIP:      geoResolver,
		RoomTTL:    cfg.RoomTTL,
		StunAddr:   cfg.StunListen,
		StunPublic: cfg.StunPublic,
		PublicURL:  cfg.PublicBaseURL,
	}

	return server.Run(rootCtx, server.Options{
		Listen:  cfg.Listen,
		Logger:  logger,
		APIDeps: apiDeps,
	})
}

func openStore(cfg *config.Config, logger *slog.Logger) (store.Store, error) {
	switch cfg.StorageDriver {
	case "memory":
		logger.Info("storage: in-memory (rooms will not persist across restarts)")
		return store.NewMemoryStore(), nil
	case "sqlite":
		db, err := sql.Open("sqlite", cfg.StorageDSN)
		if err != nil {
			return nil, err
		}
		db.SetMaxOpenConns(1)
		if err := db.Ping(); err != nil {
			db.Close()
			return nil, err
		}
		return store.NewSQLStore(db, store.DialectSQLite)
	case "mysql":
		db, err := sql.Open("mysql", cfg.StorageDSN)
		if err != nil {
			return nil, err
		}
		db.SetMaxOpenConns(20)
		db.SetMaxIdleConns(4)
		db.SetConnMaxLifetime(5 * time.Minute)
		if err := db.Ping(); err != nil {
			db.Close()
			return nil, err
		}
		return store.NewSQLStore(db, store.DialectMySQL)
	default:
		return nil, fmt.Errorf("unsupported storage driver %q", cfg.StorageDriver)
	}
}

func openCache(cfg *config.Config) (store.Cache, error) {
	switch cfg.CacheDriver {
	case "memory":
		return store.NewMemoryCache(), nil
	case "none":
		return nil, nil
	case "redis":
		if cfg.CacheURL == "" {
			return nil, errors.New("EASYLAN_CACHE_URL must be set when EASYLAN_CACHE_DRIVER=redis")
		}
		opt, err := redis.ParseURL(cfg.CacheURL)
		if err != nil {
			return nil, err
		}
		client := redis.NewClient(opt)
		if err := client.Ping(context.Background()).Err(); err != nil {
			client.Close()
			return nil, err
		}
		return store.NewRedisCache(client), nil
	default:
		return nil, fmt.Errorf("unsupported cache driver %q", cfg.CacheDriver)
	}
}

func pruneLoop(ctx context.Context, logger *slog.Logger, st store.Store) {
	t := time.NewTicker(60 * time.Second)
	defer t.Stop()
	for {
		select {
		case <-ctx.Done():
			return
		case <-t.C:
			n, err := st.PruneExpiredRooms(ctx, time.Now().UTC())
			if err != nil {
				logger.Warn("prune: failed", "err", err)
				continue
			}
			if n > 0 {
				logger.Info("prune: removed expired rooms", "count", n)
			}
		}
	}
}

func newLogger(level string) *slog.Logger {
	var lv slog.Level
	switch level {
	case "debug":
		lv = slog.LevelDebug
	case "warn":
		lv = slog.LevelWarn
	case "error":
		lv = slog.LevelError
	default:
		lv = slog.LevelInfo
	}
	return slog.New(slog.NewTextHandler(os.Stderr, &slog.HandlerOptions{Level: lv}))
}
