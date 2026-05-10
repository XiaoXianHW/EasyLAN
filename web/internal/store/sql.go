package store

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/xiaoxianhw/easylan/web/internal/room"
)

// Dialect is the SQL flavour used by the underlying driver.  It controls
// migration text and a couple of placeholder/quoting differences.
type Dialect string

const (
	DialectSQLite Dialect = "sqlite"
	DialectMySQL  Dialect = "mysql"
)

// SQLStore is a Store backed by database/sql.  The same instance handles
// both SQLite (testing/local) and MySQL (production) — only the dialect-
// specific migration text differs.
type SQLStore struct {
	db      *sql.DB
	dialect Dialect
}

// NewSQLStore wraps an existing *sql.DB.  Callers are responsible for
// having opened db with the matching driver.
func NewSQLStore(db *sql.DB, dialect Dialect) (*SQLStore, error) {
	if db == nil {
		return nil, errors.New("store: nil *sql.DB")
	}
	s := &SQLStore{db: db, dialect: dialect}
	if err := s.migrate(context.Background()); err != nil {
		return nil, fmt.Errorf("store: migrate: %w", err)
	}
	return s, nil
}

// Close releases the underlying database handle.
func (s *SQLStore) Close() error { return s.db.Close() }

// migrate creates the rooms table on first run.  The schema is identical
// across dialects except for the auto-increment / TEXT idiom.
func (s *SQLStore) migrate(ctx context.Context) error {
	var stmt string
	switch s.dialect {
	case DialectSQLite:
		stmt = `
CREATE TABLE IF NOT EXISTS rooms (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    code          TEXT    NOT NULL UNIQUE,
    name          TEXT    NOT NULL,
    password      TEXT    NOT NULL DEFAULT '',
    owner_token   TEXT    NOT NULL,
    owner_name    TEXT    NOT NULL DEFAULT '',
    loader        TEXT    NOT NULL,
    version       TEXT    NOT NULL,
    modes         TEXT    NOT NULL DEFAULT '',
    motd          TEXT    NOT NULL DEFAULT '',
    max_players   INTEGER NOT NULL DEFAULT 0,
    online_player INTEGER NOT NULL DEFAULT 0,
    players       TEXT    NOT NULL DEFAULT '',
    region        TEXT    NOT NULL DEFAULT '',
    host_ip       TEXT    NOT NULL DEFAULT '',
    nat_type      TEXT    NOT NULL DEFAULT '',
    host_port     INTEGER NOT NULL DEFAULT 0,
    has_password  INTEGER NOT NULL DEFAULT 0,
    is_public     INTEGER NOT NULL DEFAULT 1,
    tags          TEXT    NOT NULL DEFAULT '',
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL,
    expires_at    TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_rooms_loader_version ON rooms(loader, version);
CREATE INDEX IF NOT EXISTS idx_rooms_region        ON rooms(region);
CREATE INDEX IF NOT EXISTS idx_rooms_updated_at    ON rooms(updated_at);
`
	case DialectMySQL:
		stmt = `
CREATE TABLE IF NOT EXISTS rooms (
    id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(16)  NOT NULL,
    name          VARCHAR(128) NOT NULL,
    password      VARCHAR(128) NOT NULL DEFAULT '',
    owner_token   VARCHAR(64)  NOT NULL,
    owner_name    VARCHAR(64)  NOT NULL DEFAULT '',
    loader        VARCHAR(16)  NOT NULL,
    version       VARCHAR(32)  NOT NULL,
    modes         VARCHAR(128) NOT NULL DEFAULT '',
    motd          VARCHAR(255) NOT NULL DEFAULT '',
    max_players   INT          NOT NULL DEFAULT 0,
    online_player INT          NOT NULL DEFAULT 0,
    players       TEXT         NOT NULL,
    region        VARCHAR(64)  NOT NULL DEFAULT '',
    host_ip       VARCHAR(64)  NOT NULL DEFAULT '',
    nat_type      VARCHAR(32)  NOT NULL DEFAULT '',
    host_port     INT          NOT NULL DEFAULT 0,
    has_password  TINYINT(1)   NOT NULL DEFAULT 0,
    is_public     TINYINT(1)   NOT NULL DEFAULT 1,
    tags          VARCHAR(255) NOT NULL DEFAULT '',
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    expires_at    DATETIME     NOT NULL,
    UNIQUE KEY uk_rooms_code(code),
    KEY idx_rooms_loader_version(loader, version),
    KEY idx_rooms_region(region),
    KEY idx_rooms_updated_at(updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
`
	default:
		return fmt.Errorf("store: unsupported dialect %q", s.dialect)
	}

	for _, part := range strings.Split(stmt, ";") {
		trimmed := strings.TrimSpace(part)
		if trimmed == "" {
			continue
		}
		if _, err := s.db.ExecContext(ctx, trimmed); err != nil {
			return fmt.Errorf("store: exec %q: %w", firstLine(trimmed), err)
		}
	}
	return nil
}

func firstLine(s string) string {
	idx := strings.IndexByte(s, '\n')
	if idx < 0 {
		return s
	}
	return s[:idx]
}

// CreateRoom inserts r into the database.
func (s *SQLStore) CreateRoom(ctx context.Context, r *room.Room) error {
	encodeRoom(r)
	if r.CreatedAt.IsZero() {
		r.CreatedAt = time.Now().UTC()
	}
	r.UpdatedAt = r.CreatedAt
	_, err := s.db.ExecContext(ctx, `
INSERT INTO rooms(code,name,password,owner_token,owner_name,loader,version,modes,motd,
                  max_players,online_player,players,region,host_ip,nat_type,host_port,
                  has_password,is_public,tags,created_at,updated_at,expires_at)
VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
		r.Code, r.Name, r.Password, r.OwnerToken, r.OwnerName, string(r.Loader), r.Version,
		r.ModesRaw, r.Motd, r.MaxPlayers, r.OnlinePly, r.PlayersRaw, r.Region, r.HostIP,
		r.NATType, r.HostPort, boolToInt(r.HasPwd), boolToInt(r.IsPublic), r.TagsRaw,
		r.CreatedAt, r.UpdatedAt, r.ExpiresAt,
	)
	if err != nil {
		if isUniqueViolation(err) {
			return ErrConflict
		}
		return err
	}
	return nil
}

// GetRoomByCode returns the room with the requested code or ErrNotFound.
func (s *SQLStore) GetRoomByCode(ctx context.Context, code string) (*room.Room, error) {
	row := s.db.QueryRowContext(ctx, roomSelect+" WHERE code = ?", code)
	r, err := scanRoom(row.Scan)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, ErrNotFound
	}
	return r, err
}

// UpdateRoom persists r back to storage.  The room is matched by code.
func (s *SQLStore) UpdateRoom(ctx context.Context, r *room.Room) error {
	encodeRoom(r)
	r.UpdatedAt = time.Now().UTC()
	res, err := s.db.ExecContext(ctx, `
UPDATE rooms SET name=?,password=?,owner_name=?,loader=?,version=?,modes=?,motd=?,
                 max_players=?,online_player=?,players=?,region=?,host_ip=?,nat_type=?,
                 host_port=?,has_password=?,is_public=?,tags=?,updated_at=?,expires_at=?
WHERE code=?`,
		r.Name, r.Password, r.OwnerName, string(r.Loader), r.Version, r.ModesRaw,
		r.Motd, r.MaxPlayers, r.OnlinePly, r.PlayersRaw, r.Region, r.HostIP, r.NATType,
		r.HostPort, boolToInt(r.HasPwd), boolToInt(r.IsPublic), r.TagsRaw, r.UpdatedAt,
		r.ExpiresAt, r.Code,
	)
	if err != nil {
		return err
	}
	n, err := res.RowsAffected()
	if err != nil {
		return err
	}
	if n == 0 {
		return ErrNotFound
	}
	return nil
}

// DeleteRoom removes a room by code.
func (s *SQLStore) DeleteRoom(ctx context.Context, code string) error {
	res, err := s.db.ExecContext(ctx, `DELETE FROM rooms WHERE code = ?`, code)
	if err != nil {
		return err
	}
	n, err := res.RowsAffected()
	if err != nil {
		return err
	}
	if n == 0 {
		return ErrNotFound
	}
	return nil
}

// ListRooms applies f and returns matching rooms plus the total count
// before pagination.
func (s *SQLStore) ListRooms(ctx context.Context, f RoomFilter) ([]*room.Room, int, error) {
	var (
		clauses []string
		args    []any
	)
	if f.Loader != "" {
		clauses = append(clauses, "loader = ?")
		args = append(args, f.Loader)
	}
	if f.Version != "" {
		clauses = append(clauses, "version = ?")
		args = append(args, f.Version)
	}
	if f.Region != "" {
		clauses = append(clauses, "region LIKE ?")
		args = append(args, "%"+f.Region+"%")
	}
	if f.Search != "" {
		clauses = append(clauses, "(name LIKE ? OR motd LIKE ?)")
		args = append(args, "%"+f.Search+"%", "%"+f.Search+"%")
	}
	if f.OnlyOpen {
		clauses = append(clauses, "(max_players = 0 OR online_player < max_players)")
	}
	clauses = append(clauses, "is_public = 1")
	where := "WHERE " + strings.Join(clauses, " AND ")

	var total int
	if err := s.db.QueryRowContext(ctx, `SELECT COUNT(*) FROM rooms `+where, args...).
		Scan(&total); err != nil {
		return nil, 0, err
	}

	q := roomSelect + " " + where + " ORDER BY updated_at DESC"
	if f.Limit > 0 {
		q += fmt.Sprintf(" LIMIT %d", f.Limit)
		if f.Offset > 0 {
			q += fmt.Sprintf(" OFFSET %d", f.Offset)
		}
	}
	rows, err := s.db.QueryContext(ctx, q, args...)
	if err != nil {
		return nil, 0, err
	}
	defer rows.Close()

	var out []*room.Room
	for rows.Next() {
		r, err := scanRoom(rows.Scan)
		if err != nil {
			return nil, 0, err
		}
		out = append(out, r)
	}
	return out, total, rows.Err()
}

// TouchRoom updates expires_at and updated_at without changing other fields.
func (s *SQLStore) TouchRoom(ctx context.Context, code string, expiresAt time.Time) error {
	res, err := s.db.ExecContext(ctx,
		`UPDATE rooms SET expires_at = ?, updated_at = ? WHERE code = ?`,
		expiresAt, time.Now().UTC(), code)
	if err != nil {
		return err
	}
	n, err := res.RowsAffected()
	if err != nil {
		return err
	}
	if n == 0 {
		return ErrNotFound
	}
	return nil
}

// PruneExpiredRooms deletes rooms whose expires_at has passed.
func (s *SQLStore) PruneExpiredRooms(ctx context.Context, now time.Time) (int, error) {
	res, err := s.db.ExecContext(ctx,
		`DELETE FROM rooms WHERE expires_at <> ? AND expires_at < ?`,
		time.Time{}, now)
	if err != nil {
		return 0, err
	}
	n, err := res.RowsAffected()
	if err != nil {
		return 0, err
	}
	return int(n), nil
}

const roomSelect = `SELECT id,code,name,password,owner_token,owner_name,loader,version,modes,motd,
                          max_players,online_player,players,region,host_ip,nat_type,host_port,
                          has_password,is_public,tags,created_at,updated_at,expires_at
                   FROM rooms`

type scanFn func(dest ...any) error

func scanRoom(scan scanFn) (*room.Room, error) {
	var (
		r        room.Room
		hasPwd   int
		isPublic int
	)
	if err := scan(
		&r.ID, &r.Code, &r.Name, &r.Password, &r.OwnerToken, &r.OwnerName,
		&r.Loader, &r.Version, &r.ModesRaw, &r.Motd, &r.MaxPlayers, &r.OnlinePly,
		&r.PlayersRaw, &r.Region, &r.HostIP, &r.NATType, &r.HostPort, &hasPwd,
		&isPublic, &r.TagsRaw, &r.CreatedAt, &r.UpdatedAt, &r.ExpiresAt,
	); err != nil {
		return nil, err
	}
	r.HasPwd = hasPwd != 0
	r.IsPublic = isPublic != 0
	decodeRoom(&r)
	return &r, nil
}

func encodeRoom(r *room.Room) {
	modes := make([]string, 0, len(r.Modes))
	for _, m := range r.Modes {
		modes = append(modes, string(m))
	}
	r.ModesRaw = strings.Join(modes, ",")
	r.PlayersRaw = strings.Join(r.Players, ",")
	r.TagsRaw = strings.Join(r.Tags, ",")
}

func decodeRoom(r *room.Room) {
	if r.ModesRaw != "" {
		for _, p := range strings.Split(r.ModesRaw, ",") {
			r.Modes = append(r.Modes, room.Mode(strings.TrimSpace(p)))
		}
	}
	if r.PlayersRaw != "" {
		r.Players = splitNonEmpty(r.PlayersRaw, ",")
	} else {
		r.Players = []string{}
	}
	if r.TagsRaw != "" {
		r.Tags = splitNonEmpty(r.TagsRaw, ",")
	} else {
		r.Tags = []string{}
	}
}

func splitNonEmpty(s, sep string) []string {
	parts := strings.Split(s, sep)
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			out = append(out, p)
		}
	}
	return out
}

func boolToInt(b bool) int {
	if b {
		return 1
	}
	return 0
}

func isUniqueViolation(err error) bool {
	if err == nil {
		return false
	}
	msg := err.Error()
	return strings.Contains(msg, "UNIQUE constraint") ||
		strings.Contains(msg, "Duplicate entry") ||
		strings.Contains(msg, "1062")
}
