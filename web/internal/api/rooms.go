package api

import (
	"errors"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/go-chi/chi/v5"

	"github.com/xiaoxianhw/easylan/web/internal/room"
	"github.com/xiaoxianhw/easylan/web/internal/store"
)

// CreateRoomRequest is the JSON payload accepted by POST /api/rooms.
type CreateRoomRequest struct {
	Name       string      `json:"name"`
	Password   string      `json:"password,omitempty"`
	OwnerName  string      `json:"ownerName"`
	Loader     room.Loader `json:"loader"`
	Version    string      `json:"version"`
	Modes      []room.Mode `json:"modes"`
	Motd       string      `json:"motd"`
	MaxPlayers int         `json:"maxPlayers"`
	HostPort   int         `json:"hostPort"`
	IsPublic   *bool       `json:"isPublic,omitempty"`
	Tags       []string    `json:"tags,omitempty"`
}

// CreateRoomResponse is returned to the room creator only.  It includes
// the OwnerToken that subsequent updates / deletes must present.
type CreateRoomResponse struct {
	Code       string     `json:"code"`
	OwnerToken string     `json:"ownerToken"`
	Room       *room.Room `json:"room"`
}

// CreateRoom handles POST /api/rooms.
func (h *Handler) CreateRoom(w http.ResponseWriter, r *http.Request) {
	var req CreateRoomRequest
	if err := decodeJSON(r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "bad_request", err.Error())
		return
	}
	if strings.TrimSpace(req.Name) == "" {
		writeError(w, http.StatusBadRequest, "missing_name", "room name is required")
		return
	}
	if req.Loader == "" || req.Version == "" {
		writeError(w, http.StatusBadRequest, "missing_loader_version", "loader and version are required")
		return
	}

	code, err := room.GenerateCode()
	if err != nil {
		writeError(w, http.StatusInternalServerError, "code_gen", err.Error())
		return
	}
	owner, err := randomToken(16)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "token_gen", err.Error())
		return
	}

	isPublic := true
	if req.IsPublic != nil {
		isPublic = *req.IsPublic
	}

	hostIP := clientIP(r)
	loc, _ := h.deps.GeoIP.Lookup(hostIP)
	regionDisplay := strings.TrimSpace(strings.Join(filterEmpty([]string{loc.Region, loc.City}), "·"))
	if regionDisplay == "" {
		regionDisplay = loc.Country
	}

	rec := &room.Room{
		Code:       code,
		Name:       strings.TrimSpace(req.Name),
		Password:   req.Password,
		HasPwd:     req.Password != "",
		OwnerToken: owner,
		OwnerName:  strings.TrimSpace(req.OwnerName),
		Loader:     req.Loader,
		Version:    strings.TrimSpace(req.Version),
		Modes:      req.Modes,
		Motd:       strings.TrimSpace(req.Motd),
		MaxPlayers: req.MaxPlayers,
		HostPort:   req.HostPort,
		HostIP:     hostIP,
		Region:     regionDisplay,
		IsPublic:   isPublic,
		Tags:       req.Tags,
		ExpiresAt:  time.Now().Add(h.deps.RoomTTL).UTC(),
	}

	ctx, cancel := ctxWithTimeout(r, 5*time.Second)
	defer cancel()
	if err := h.deps.Store.CreateRoom(ctx, rec); err != nil {
		writeError(w, http.StatusInternalServerError, "create_failed", err.Error())
		return
	}
	pub := rec.Public()
	writeJSON(w, http.StatusCreated, CreateRoomResponse{
		Code:       code,
		OwnerToken: owner,
		Room:       &pub,
	})
}

// ListRooms handles GET /api/rooms.
func (h *Handler) ListRooms(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	limit, _ := strconv.Atoi(q.Get("limit"))
	if limit <= 0 || limit > 100 {
		limit = 25
	}
	offset, _ := strconv.Atoi(q.Get("offset"))
	if offset < 0 {
		offset = 0
	}

	filter := store.RoomFilter{
		Loader:   q.Get("loader"),
		Version:  q.Get("version"),
		Region:   q.Get("region"),
		Search:   q.Get("q"),
		OnlyOpen: q.Get("open") == "1",
		Limit:    limit,
		Offset:   offset,
	}

	ctx, cancel := ctxWithTimeout(r, 5*time.Second)
	defer cancel()
	items, total, err := h.deps.Store.ListRooms(ctx, filter)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "list_failed", err.Error())
		return
	}

	out := make([]room.Room, 0, len(items))
	for _, it := range items {
		out = append(out, it.Public())
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"items":  out,
		"total":  total,
		"limit":  limit,
		"offset": offset,
	})
}

// GetRoom handles GET /api/rooms/{code}.
func (h *Handler) GetRoom(w http.ResponseWriter, r *http.Request) {
	code := room.NormalizeCode(chi.URLParam(r, "code"))
	if err := room.ValidateCode(code); err != nil {
		writeError(w, http.StatusBadRequest, "bad_code", err.Error())
		return
	}
	ctx, cancel := ctxWithTimeout(r, 5*time.Second)
	defer cancel()
	rec, err := h.deps.Store.GetRoomByCode(ctx, code)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			writeError(w, http.StatusNotFound, "not_found", "room not found")
			return
		}
		writeError(w, http.StatusInternalServerError, "lookup_failed", err.Error())
		return
	}
	pub := rec.Public()
	writeJSON(w, http.StatusOK, pub)
}

// JoinRoomRequest is the payload for POST /api/rooms/{code}/join.
type JoinRoomRequest struct {
	Password string `json:"password,omitempty"`
}

// JoinResponse is returned by JoinRoom.  We do not surface the host IP
// directly — instead clients get a signed pointer they can use against
// our signaling endpoint.
type JoinResponse struct {
	Code     string      `json:"code"`
	Loader   room.Loader `json:"loader"`
	Version  string      `json:"version"`
	Modes    []room.Mode `json:"modes"`
	Motd     string      `json:"motd"`
	HostPort int         `json:"hostPort"`
	NATType  string      `json:"natType"`
	Region   string      `json:"region"`
}

// JoinRoom handles POST /api/rooms/{code}/join.  It validates the room
// code + password and returns the public connection metadata.
func (h *Handler) JoinRoom(w http.ResponseWriter, r *http.Request) {
	code := room.NormalizeCode(chi.URLParam(r, "code"))
	if err := room.ValidateCode(code); err != nil {
		writeError(w, http.StatusBadRequest, "bad_code", err.Error())
		return
	}
	var req JoinRoomRequest
	if r.ContentLength > 0 {
		_ = decodeJSON(r, &req)
	}
	ctx, cancel := ctxWithTimeout(r, 5*time.Second)
	defer cancel()
	rec, err := h.deps.Store.GetRoomByCode(ctx, code)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			writeError(w, http.StatusNotFound, "not_found", "room not found")
			return
		}
		writeError(w, http.StatusInternalServerError, "lookup_failed", err.Error())
		return
	}
	if rec.HasPwd && rec.Password != req.Password {
		writeError(w, http.StatusForbidden, "bad_password", "incorrect room password")
		return
	}
	if rec.MaxPlayers > 0 && rec.OnlinePly >= rec.MaxPlayers {
		writeError(w, http.StatusConflict, "room_full", "room is full")
		return
	}
	resp := JoinResponse{
		Code:     rec.Code,
		Loader:   rec.Loader,
		Version:  rec.Version,
		Modes:    rec.Modes,
		Motd:     rec.Motd,
		HostPort: rec.HostPort,
		NATType:  rec.NATType,
		Region:   rec.Region,
	}
	writeJSON(w, http.StatusOK, resp)
}

// HeartbeatRequest renews the room TTL and refreshes runtime fields.
type HeartbeatRequest struct {
	OwnerToken   string   `json:"ownerToken"`
	Players      []string `json:"players"`
	OnlinePlayer int      `json:"onlinePlayer"`
	MaxPlayers   int      `json:"maxPlayers"`
	NATType      string   `json:"natType"`
	Motd         string   `json:"motd"`
}

// Heartbeat handles POST /api/rooms/{code}/heartbeat.  The mod-side
// agent calls this periodically to update player list and renew the
// expires_at deadline.
func (h *Handler) Heartbeat(w http.ResponseWriter, r *http.Request) {
	code := room.NormalizeCode(chi.URLParam(r, "code"))
	if err := room.ValidateCode(code); err != nil {
		writeError(w, http.StatusBadRequest, "bad_code", err.Error())
		return
	}
	var req HeartbeatRequest
	if err := decodeJSON(r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "bad_request", err.Error())
		return
	}
	ctx, cancel := ctxWithTimeout(r, 5*time.Second)
	defer cancel()
	rec, err := h.deps.Store.GetRoomByCode(ctx, code)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			writeError(w, http.StatusNotFound, "not_found", "room not found")
			return
		}
		writeError(w, http.StatusInternalServerError, "lookup_failed", err.Error())
		return
	}
	if rec.OwnerToken != req.OwnerToken {
		writeError(w, http.StatusForbidden, "bad_owner", "owner token mismatch")
		return
	}
	if req.MaxPlayers > 0 {
		rec.MaxPlayers = req.MaxPlayers
	}
	rec.OnlinePly = req.OnlinePlayer
	if req.Motd != "" {
		rec.Motd = req.Motd
	}
	if req.NATType != "" {
		rec.NATType = req.NATType
	}
	if req.Players != nil {
		rec.Players = req.Players
	}
	rec.ExpiresAt = time.Now().Add(h.deps.RoomTTL).UTC()
	if err := h.deps.Store.UpdateRoom(ctx, rec); err != nil {
		writeError(w, http.StatusInternalServerError, "update_failed", err.Error())
		return
	}
	pub := rec.Public()
	writeJSON(w, http.StatusOK, pub)
}

// DeleteRoom handles DELETE /api/rooms/{code}.  Requires the owner
// token via X-Owner-Token header.
func (h *Handler) DeleteRoom(w http.ResponseWriter, r *http.Request) {
	code := room.NormalizeCode(chi.URLParam(r, "code"))
	if err := room.ValidateCode(code); err != nil {
		writeError(w, http.StatusBadRequest, "bad_code", err.Error())
		return
	}
	owner := r.Header.Get("X-Owner-Token")
	if owner == "" {
		writeError(w, http.StatusUnauthorized, "missing_owner", "owner token required")
		return
	}
	ctx, cancel := ctxWithTimeout(r, 5*time.Second)
	defer cancel()
	rec, err := h.deps.Store.GetRoomByCode(ctx, code)
	if err != nil {
		if errors.Is(err, store.ErrNotFound) {
			writeError(w, http.StatusNotFound, "not_found", "room not found")
			return
		}
		writeError(w, http.StatusInternalServerError, "lookup_failed", err.Error())
		return
	}
	if rec.OwnerToken != owner {
		writeError(w, http.StatusForbidden, "bad_owner", "owner token mismatch")
		return
	}
	if err := h.deps.Store.DeleteRoom(ctx, code); err != nil {
		writeError(w, http.StatusInternalServerError, "delete_failed", err.Error())
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func filterEmpty(in []string) []string {
	out := in[:0]
	for _, s := range in {
		if strings.TrimSpace(s) != "" {
			out = append(out, s)
		}
	}
	return out
}
