# EasyLAN — Web Platform

Single-binary Go service that pairs with the EasyLAN Minecraft mod to
match players into LAN-style rooms over P2P, FRP or IPv6 connections.

* **Backend** — Go HTTP API + UDP STUN server
* **Frontend** — Next.js (static export) embedded inside the Go binary
* **Storage** — SQLite (default), MySQL 5.7 (production), Redis (cache)
* **Single command** — `./easylan-web` serves the API, the STUN
  responder and the UI from one process.

```
$ ./easylan-web
http: listening :8080
stun: listening :3478
storage: sqlite easylan.db
cache: memory
ready.
```

---

## Architecture

```
┌──────────────────────────┐    ┌────────────────────────┐
│  Next.js frontend        │    │  EasyLAN mod (Forge /  │
│  (homepage, rooms,       │    │   Fabric / NeoForge)   │
│   downloads, docs)       │    │                        │
│                          │    │  /status               │
│  /api/* fetch + WebRTC   │    │  /playerlist           │
└────────────┬─────────────┘    └────────────┬───────────┘
             │ JSON / WebRTC                 │ HTTP
             ▼                               ▼
       ┌────────────────────────────────────────┐
       │  easylan-web (Go single binary)        │
       │  ─ chi router under /api               │
       │  ─ go:embed Next.js static export      │
       │  ─ pion/stun UDP server (:3478)        │
       │  ─ NAT classification (RFC 5780-ish)   │
       │  ─ ip2region GeoIP (province / city)   │
       └─────────┬─────────────────────────────┘
                 │
        ┌────────┴─────────┐
        ▼                  ▼
   ┌─────────┐       ┌──────────┐
   │ MySQL / │       │  Redis   │
   │ SQLite  │       │ (cache)  │
   └─────────┘       └──────────┘
```

### Layout

```
web/
├── main.go                       # entry point
├── internal/
│   ├── api/                      # HTTP handlers under /api
│   ├── config/                   # env-driven configuration
│   ├── frontend/                 # //go:embed Next.js dist
│   ├── geoip/                    # ip2region resolver
│   ├── mod/                      # client for the mod's HTTP API
│   ├── nat/                      # NAT type classifier
│   ├── room/                     # core domain types + code generation
│   ├── server/                   # http server boot + middleware
│   ├── store/                    # storage interface + SQL/Redis/memory
│   ├── stun/                     # pion/stun server wrapper
│   └── version/                  # supported MC version catalogue
└── frontend/                     # Next.js source (built into dist/)
```

---

## Quickstart

```bash
# 1. install Go 1.23+, Node 20+
# 2. build the frontend AND the binary in one go
cd web
make build

# 3. run with SQLite + in-memory cache
./easylan-web
# → open http://localhost:8080
```

For production-style local testing with MySQL + Redis:

```bash
make docker-up
EASYLAN_STORAGE_DRIVER=mysql \
EASYLAN_STORAGE_DSN="easylan:easylan@tcp(127.0.0.1:3306)/easylan?parseTime=true&charset=utf8mb4" \
EASYLAN_CACHE_DRIVER=redis \
EASYLAN_CACHE_URL=redis://127.0.0.1:6379/0 \
./easylan-web
```

---

## Configuration

All settings come from environment variables.  Defaults work out of the
box — only set what you need to override.

| Variable                    | Default            | Notes |
|-----------------------------|--------------------|-------|
| `EASYLAN_LISTEN`            | `:8080`            | HTTP API + frontend |
| `EASYLAN_STUN_LISTEN`       | `:3478`            | UDP STUN bind |
| `EASYLAN_STUN_PUBLIC`       | *(none)*           | Public `host:port` advertised in `/api/ip` (e.g. `stun.example.com:3478`) |
| `EASYLAN_STORAGE_DRIVER`    | `sqlite`           | `sqlite`, `mysql`, `memory` |
| `EASYLAN_STORAGE_DSN`       | `easylan.db`       | SQLite path or MySQL DSN |
| `EASYLAN_CACHE_DRIVER`      | `memory`           | `memory`, `redis`, `none` |
| `EASYLAN_CACHE_URL`         | *(none)*           | `redis://localhost:6379/0` |
| `EASYLAN_GEOIP_PATH`        | *(none)*           | path to `ip2region.xdb` |
| `EASYLAN_PUBLIC_BASE_URL`   | *(none)*           | external base URL of this server |
| `EASYLAN_ROOM_TTL_SECONDS`  | `900`              | room expiry (heartbeat refreshes it) |
| `EASYLAN_LOG_LEVEL`         | `info`             | `debug`, `info`, `warn`, `error` |
| `EASYLAN_ENV`               | `development`      | free-form env tag |

> The GeoIP database is **optional**.  Without `ip2region.xdb` the API
> still works but cannot resolve province/city — public IPs fall back
> to "Unknown" and private/loopback IPs to "Local".  Grab the latest
> [ip2region](https://github.com/lionsoul2014/ip2region) `xdb` build
> for accurate Chinese province/city resolution.

---

## HTTP API

| Method | Path                              | Description                                           |
|--------|-----------------------------------|-------------------------------------------------------|
| GET    | `/api/health`                     | Liveness + GeoIP availability                          |
| GET    | `/api/ip`                         | Caller's IP, GeoIP region, recommended STUN servers    |
| POST   | `/api/nat/probe`                  | Classify NAT type from WebRTC + STUN observations      |
| POST   | `/api/rooms`                      | Create a room — returns `code` + private `ownerToken`  |
| GET    | `/api/rooms`                      | List public rooms (filters: `loader`, `version`, `region`, `q`, `open`, `limit`, `offset`) |
| GET    | `/api/rooms/{code}`               | Fetch room details                                     |
| POST   | `/api/rooms/{code}/join`          | Validate password and return connection metadata       |
| POST   | `/api/rooms/{code}/heartbeat`     | Owner-only: refresh expiry + replace player list       |
| DELETE | `/api/rooms/{code}`               | Owner-only (`X-Owner-Token` header)                    |
| POST   | `/api/mod/status`                 | Admin/debug: scrape the mod's `/status` + `/playerlist` for a given `baseUrl` |
| GET    | `/api/downloads`                  | Curated catalogue of supported MC versions             |

Room codes are formatted as `XXXXX-XXXXX` from a 31-character alphabet
that excludes ambiguous glyphs (`0/O/I/L/1`).

---

## NAT detection

The classification combines **server-side STUN observation** with
**client-side WebRTC ICE candidates**:

| Observation                                    | Classification          | Recommended mode |
|------------------------------------------------|-------------------------|------------------|
| Client reports IPv6                            | `ipv6`                  | direct IPv6 |
| `webrtcNatType=host` + matching IP/port        | `open-internet`         | direct |
| `webrtcNatType=srflx` + matching IP/port       | `full-cone`             | P2P |
| `webrtcNatType=prflx` + matching IP/port       | `restricted-cone`       | P2P (with retries) |
| Client/server-observed IP or port differs      | `symmetric`             | FRP / TURN |
| `udpSupported=false`                           | `udp-blocked`           | FRP |
| `webrtcNatType=relay`                          | `symmetric`             | TURN |

The frontend bootstraps WebRTC against the STUN URLs returned from
`/api/ip` (our own `EASYLAN_STUN_PUBLIC` plus a couple of public Google
/ Cloudflare fallbacks).

---

## Mod integration

The mod (in this repo's other branches) already exposes a localhost
HTTP API on `127.0.0.1:28960`:

```
GET /status      → { port, version, owner, motd, pvp, onlineMode,
                     spawnAnimals, allowFlight, difficulty, gameType,
                     maxPlayer, onlinePlayer }
GET /playerlist  → ["alice", "bob", …]
```

The `internal/mod` package contains a Go client for this API.  A future
mod-side companion can:

1. Generate a room on the platform via `POST /api/rooms`
2. Periodically `POST /api/rooms/{code}/heartbeat` with the live player
   list scraped from `/playerlist` and the NAT type classified from the
   server's observation of the host.
3. Delete the room on graceful shutdown.

The `POST /api/mod/status` admin endpoint is a useful way to verify the
mod is reachable while debugging.

---

## Frontend

The frontend is a Next.js 14 app that we build with `next build` in
`output: "export"` mode and copy into `internal/frontend/dist/`.  The
Go binary then `//go:embed`s that directory and serves it at `/`.  In
development you can run the API on `:8080` and the frontend on `:3000`
separately:

```bash
# terminal 1
cd web && go run ./

# terminal 2
cd web/frontend && npm run dev
```

The frontend reads from `window.location.origin`, so when running the
production single-binary build no extra configuration is needed.

### Pages

* `/` — homepage, hero, NAT detection card, feature overview
* `/rooms` — room list with loader / search filters
* `/rooms/view?code=XXXXX-XXXXX` — single room detail + join flow
* `/download` — Forge / Fabric / NeoForge version matrix → CurseForge / Modrinth
* `/docs` — operator + integrator documentation

The visual language draws from the Ollama (rounded pills, white-on-black
minimalism) and xAI (monospace display, dark canvas, brutalist accents)
references curated in
[VoltAgent/awesome-design-md](https://github.com/VoltAgent/awesome-design-md).

---

## Tests

```bash
go test ./...
```

Covers room-code generation/validation, NAT classification, and SQL
storage CRUD + TTL pruning.
