package api

import (
	"net/http"

	"github.com/xiaoxianhw/easylan/web/internal/mod"
)

// ModProxyRequest tells the server which local EasyLAN HTTP API to scrape.
type ModProxyRequest struct {
	BaseURL string `json:"baseUrl"`
}

// ModStatus handles POST /api/mod/status — admin / debug only.  The
// caller can point at any reachable EasyLAN HTTP API endpoint and get
// back the parsed status snapshot + raw key/value map.
func (h *Handler) ModStatus(w http.ResponseWriter, r *http.Request) {
	var req ModProxyRequest
	if err := decodeJSON(r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "bad_request", err.Error())
		return
	}
	if req.BaseURL == "" {
		writeError(w, http.StatusBadRequest, "missing_base_url", "baseUrl is required")
		return
	}
	c := mod.NewClient(req.BaseURL)
	status, raw, err := c.Status(r.Context())
	if err != nil {
		writeError(w, http.StatusBadGateway, "mod_unreachable", err.Error())
		return
	}
	players, err := c.PlayerList(r.Context())
	if err != nil {
		writeError(w, http.StatusBadGateway, "mod_unreachable", err.Error())
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"status":     status,
		"statusRaw":  raw,
		"playerList": players,
	})
}
