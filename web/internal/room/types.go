// Package room defines core types and helpers for EasyLAN rooms.
package room

import "time"

// Mode is the connectivity mode advertised by a room owner.
//
// Rooms can support multiple modes simultaneously; clients pick whichever
// mode they can negotiate based on their NAT environment.
type Mode string

const (
	ModeP2P    Mode = "p2p"    // STUN-assisted UDP hole punching
	ModeFRP    Mode = "frp"    // FRP-based TCP/UDP relay
	ModeIPv6   Mode = "ipv6"   // direct IPv6 connection
	ModeRelay  Mode = "relay"  // generic relay (TURN-style)
	ModeDirect Mode = "direct" // public-IP / port-forwarded direct connect
)

// Loader identifies the Minecraft mod loader the room is using.
type Loader string

const (
	LoaderForge    Loader = "forge"
	LoaderFabric   Loader = "fabric"
	LoaderNeoForge Loader = "neoforge"
	LoaderVanilla  Loader = "vanilla"
)

// ServerStatus mirrors the JSON shape of EasyLAN's mod-side
// `GET /status` endpoint.  All values are stringified by the mod.
//
// See shared-source/core/.../net/LocalHttpApiServer.java.
type ServerStatus struct {
	Port         string `json:"port"`
	Version      string `json:"version"`
	Owner        string `json:"owner"`
	Motd         string `json:"motd"`
	PVP          string `json:"pvp"`
	OnlineMode   string `json:"onlineMode"`
	SpawnAnimals string `json:"spawnAnimals"`
	AllowFlight  string `json:"allowFlight"`
	Difficulty   string `json:"difficulty"`
	GameType     string `json:"gameType"`
	MaxPlayer    string `json:"maxPlayer"`
	OnlinePlayer string `json:"onlinePlayer"`
}

// Room describes a single LAN room exposed by the platform.
//
// A Room is identified by a human-friendly Code (XXXXX-XXXXX) and
// optionally locked behind a Password.  The OwnerToken is a private
// secret returned only to the room creator and required to update or
// delete the room.
type Room struct {
	ID         int64     `json:"-"            db:"id"`
	Code       string    `json:"code"         db:"code"`
	Name       string    `json:"name"         db:"name"`
	Password   string    `json:"-"            db:"password"`
	OwnerToken string    `json:"-"            db:"owner_token"`
	OwnerName  string    `json:"ownerName"    db:"owner_name"`
	Loader     Loader    `json:"loader"       db:"loader"`
	Version    string    `json:"version"      db:"version"`
	Modes      []Mode    `json:"modes"        db:"-"`
	ModesRaw   string    `json:"-"            db:"modes"`
	Motd       string    `json:"motd"         db:"motd"`
	MaxPlayers int       `json:"maxPlayers"   db:"max_players"`
	OnlinePly  int       `json:"onlinePlayer" db:"online_player"`
	Players    []string  `json:"players"      db:"-"`
	PlayersRaw string    `json:"-"            db:"players"`
	Region     string    `json:"region"       db:"region"`
	HostIP     string    `json:"-"            db:"host_ip"`
	NATType    string    `json:"natType"      db:"nat_type"`
	HostPort   int       `json:"hostPort"     db:"host_port"`
	HasPwd     bool      `json:"hasPassword"  db:"has_password"`
	IsPublic   bool      `json:"isPublic"     db:"is_public"`
	Tags       []string  `json:"tags"         db:"-"`
	TagsRaw    string    `json:"-"            db:"tags"`
	CreatedAt  time.Time `json:"createdAt"    db:"created_at"`
	UpdatedAt  time.Time `json:"updatedAt"    db:"updated_at"`
	ExpiresAt  time.Time `json:"expiresAt"    db:"expires_at"`
}

// Public returns a copy of r safe to expose over the public API.
func (r Room) Public() Room {
	r.Password = ""
	r.OwnerToken = ""
	r.HostIP = ""
	if r.Players == nil {
		r.Players = []string{}
	}
	if r.Modes == nil {
		r.Modes = []Mode{}
	}
	if r.Tags == nil {
		r.Tags = []string{}
	}
	return r
}
