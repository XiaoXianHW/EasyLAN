// Package mod talks to a running EasyLAN mod's HTTP API.
//
// The mod listens on 127.0.0.1:28960 by default and exposes:
//
//	GET /status      → {<key>: <value>, ...}  (see ServerStatus)
//	GET /playerlist  → ["player1", "player2", ...]
//
// Because the mod always binds to localhost, the typical deployment
// pattern is "user runs mod on the same machine that hosts the web
// platform's room agent" — but the client is generic enough to point at
// any reachable URL (for self-hosted forwarders, debugging, etc.).
package mod

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net"
	"net/http"
	"strconv"
	"time"

	"github.com/xiaoxianhw/easylan/web/internal/room"
)

// Client is a thin wrapper around http.Client targeted at a single mod
// HTTP API base URL.
type Client struct {
	BaseURL string
	HTTP    *http.Client
}

// NewClient returns a Client targeting baseURL (eg. "http://127.0.0.1:28960").
func NewClient(baseURL string) *Client {
	return &Client{
		BaseURL: baseURL,
		HTTP: &http.Client{
			Timeout: 5 * time.Second,
			Transport: &http.Transport{
				DialContext: (&net.Dialer{Timeout: 2 * time.Second}).DialContext,
			},
		},
	}
}

// Status fetches /status and returns the parsed snapshot.
func (c *Client) Status(ctx context.Context) (room.ServerStatus, map[string]string, error) {
	raw, err := c.fetch(ctx, "/status")
	if err != nil {
		return room.ServerStatus{}, nil, err
	}
	var generic map[string]string
	if err := json.Unmarshal(raw, &generic); err != nil {
		return room.ServerStatus{}, nil, fmt.Errorf("mod: parse /status: %w", err)
	}
	st := room.ServerStatus{
		Port:         generic["port"],
		Version:      generic["version"],
		Owner:        generic["owner"],
		Motd:         generic["motd"],
		PVP:          generic["pvp"],
		OnlineMode:   generic["onlineMode"],
		SpawnAnimals: generic["spawnAnimals"],
		AllowFlight:  generic["allowFlight"],
		Difficulty:   generic["difficulty"],
		GameType:     generic["gameType"],
		MaxPlayer:    generic["maxPlayer"],
		OnlinePlayer: generic["onlinePlayer"],
	}
	return st, generic, nil
}

// PlayerList fetches /playerlist and returns the parsed slice.
func (c *Client) PlayerList(ctx context.Context) ([]string, error) {
	raw, err := c.fetch(ctx, "/playerlist")
	if err != nil {
		return nil, err
	}
	var players []string
	if err := json.Unmarshal(raw, &players); err != nil {
		return nil, fmt.Errorf("mod: parse /playerlist: %w", err)
	}
	return players, nil
}

func (c *Client) fetch(ctx context.Context, path string) ([]byte, error) {
	if c.BaseURL == "" {
		return nil, errors.New("mod: empty BaseURL")
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, c.BaseURL+path, nil)
	if err != nil {
		return nil, err
	}
	resp, err := c.HTTP.Do(req)
	if err != nil {
		return nil, fmt.Errorf("mod: GET %s: %w", path, err)
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("mod: GET %s: status %d", path, resp.StatusCode)
	}
	return io.ReadAll(resp.Body)
}

// FillRoomFromStatus copies values out of an EasyLAN status map into r.
// Numeric fields are parsed best-effort — invalid values stay zero.
func FillRoomFromStatus(r *room.Room, status map[string]string) {
	if v := status["motd"]; v != "" {
		r.Motd = v
	}
	if v := status["owner"]; v != "" && r.OwnerName == "" {
		r.OwnerName = v
	}
	if v := status["version"]; v != "" && r.Version == "" {
		r.Version = v
	}
	if v, err := strconv.Atoi(status["maxPlayer"]); err == nil {
		r.MaxPlayers = v
	}
	if v, err := strconv.Atoi(status["onlinePlayer"]); err == nil {
		r.OnlinePly = v
	}
	if v, err := strconv.Atoi(status["port"]); err == nil {
		r.HostPort = v
	}
}
