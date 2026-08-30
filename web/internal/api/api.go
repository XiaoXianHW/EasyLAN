// Package api implements the REST + WebSocket layer of the EasyLAN web
// platform.  The package is organised by resource:
//
//   - api.go    — shared helpers (json encoding, request-id middleware…)
//   - rooms.go  — /api/rooms CRUD
//   - nat.go    — /api/nat/probe + /api/ip
//   - downloads.go — /api/downloads + /api/versions
//   - modproxy.go — /api/mod/* admin endpoints
package api

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"log/slog"
	"net"
	"net/http"
	"strings"
	"time"

	"github.com/xiaoxianhw/easylan/web/internal/geoip"
	"github.com/xiaoxianhw/easylan/web/internal/store"
)

// Deps bundles everything the API handlers need.
type Deps struct {
	Logger     *slog.Logger
	Store      store.Store
	Cache      store.Cache
	GeoIP      *geoip.Resolver
	RoomTTL    time.Duration
	StunAddr   string // bind address, e.g. ":3478"
	StunPublic string // public host:port advertised to clients
	PublicURL  string
}

// Handler is the API surface.  Constructed once at startup and mounted
// under /api in server.go.
type Handler struct {
	deps Deps
}

// NewHandler returns a Handler bound to deps.
func NewHandler(deps Deps) *Handler {
	if deps.Logger == nil {
		deps.Logger = slog.Default()
	}
	return &Handler{deps: deps}
}

func writeJSON(w http.ResponseWriter, status int, body any) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.Header().Set("Cache-Control", "no-store")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(body)
}

func writeError(w http.ResponseWriter, status int, code, msg string) {
	writeJSON(w, status, map[string]any{
		"error":   true,
		"code":    code,
		"message": msg,
	})
}

func decodeJSON(r *http.Request, dst any) error {
	dec := json.NewDecoder(r.Body)
	dec.DisallowUnknownFields()
	return dec.Decode(dst)
}

// clientIP returns the best-effort public IP for the request.  Trusts
// X-Forwarded-For / X-Real-IP only when the immediate hop is loopback,
// which is the common reverse-proxy case.
func clientIP(r *http.Request) string {
	host, _, err := net.SplitHostPort(r.RemoteAddr)
	if err != nil {
		host = r.RemoteAddr
	}
	if isLoopback(host) {
		if v := r.Header.Get("X-Forwarded-For"); v != "" {
			parts := strings.Split(v, ",")
			return strings.TrimSpace(parts[0])
		}
		if v := r.Header.Get("X-Real-Ip"); v != "" {
			return strings.TrimSpace(v)
		}
	}
	return host
}

func isLoopback(host string) bool {
	ip := net.ParseIP(host)
	if ip == nil {
		return host == "localhost"
	}
	return ip.IsLoopback() || ip.IsPrivate() || ip.IsLinkLocalUnicast()
}

// randomToken returns a hex-encoded random token of n bytes.
func randomToken(n int) (string, error) {
	buf := make([]byte, n)
	if _, err := rand.Read(buf); err != nil {
		return "", err
	}
	return hex.EncodeToString(buf), nil
}

// ctxWithTimeout returns a request-scoped derived context with t.
func ctxWithTimeout(r *http.Request, t time.Duration) (context.Context, context.CancelFunc) {
	return context.WithTimeout(r.Context(), t)
}
