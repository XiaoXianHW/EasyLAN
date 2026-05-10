package api

import (
	"net/http"
	"time"

	"github.com/go-chi/chi/v5"
)

// Routes returns a chi.Router covering the entire /api/* surface.
func (h *Handler) Routes() chi.Router {
	r := chi.NewRouter()

	r.Get("/health", h.health)

	r.Get("/ip", h.IP)
	r.Post("/nat/probe", h.NATProbe)

	r.Route("/rooms", func(r chi.Router) {
		r.Get("/", h.ListRooms)
		r.Post("/", h.CreateRoom)
		r.Get("/{code}", h.GetRoom)
		r.Post("/{code}/join", h.JoinRoom)
		r.Post("/{code}/heartbeat", h.Heartbeat)
		r.Delete("/{code}", h.DeleteRoom)
	})

	r.Get("/downloads", h.Downloads)
	r.Post("/mod/status", h.ModStatus)

	return r
}

func (h *Handler) health(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, map[string]any{
		"status":    "ok",
		"timestamp": time.Now().UTC().Format(time.RFC3339),
		"geoip":     h.deps.GeoIP.HasDatabase(),
	})
}
