// Package geoip resolves an IP address to a coarse geographical region.
//
// The default implementation uses ip2region's xdb format
// (https://github.com/lionsoul2014/ip2region) which ships province- and
// city-level data for both Chinese and international IP ranges in a
// single ~12 MB binary.  When the xdb file is not configured we fall
// back to a "Unknown" lookup that still returns deterministic struct
// values so callers do not need to special-case nil.
package geoip

import (
	"errors"
	"fmt"
	"net"
	"strings"
	"sync"

	"github.com/lionsoul2014/ip2region/binding/golang/xdb"
)

// Location is the resolved geographical metadata for an IP address.
type Location struct {
	IP       string `json:"ip"`
	Country  string `json:"country"`
	Region   string `json:"region"`   // 省 (provinces) for CN, otherwise state/region
	Province string `json:"province"` // alias of Region for clarity in CN context
	City     string `json:"city"`
	ISP      string `json:"isp"`
	Display  string `json:"display"` // human-readable summary
	IsLocal  bool   `json:"isLocal"` // true for RFC1918 / loopback
	IsIPv6   bool   `json:"isIPv6"`
}

// Resolver resolves IPs to Location values.
type Resolver struct {
	mu       sync.RWMutex
	searcher *xdb.Searcher
	hasDB    bool
}

// NewResolver loads the xdb file at xdbPath if it is non-empty.  When
// xdbPath is empty the resolver still works but returns "Unknown" for
// all non-local IPs.
func NewResolver(xdbPath string) (*Resolver, error) {
	r := &Resolver{}
	if strings.TrimSpace(xdbPath) == "" {
		return r, nil
	}
	buf, err := xdb.LoadContentFromFile(xdbPath)
	if err != nil {
		return nil, fmt.Errorf("geoip: load %q: %w", xdbPath, err)
	}
	searcher, err := xdb.NewWithBuffer(xdb.IPv4, buf)
	if err != nil {
		return nil, fmt.Errorf("geoip: searcher: %w", err)
	}
	r.searcher = searcher
	r.hasDB = true
	return r, nil
}

// Lookup returns the resolved Location for ip.  ip may be empty in which
// case the local Location is returned.
func (r *Resolver) Lookup(ip string) (Location, error) {
	loc := Location{IP: ip}
	if ip == "" {
		loc.IsLocal = true
		loc.Display = "Local"
		return loc, nil
	}
	parsed := net.ParseIP(ip)
	if parsed == nil {
		return loc, errors.New("geoip: invalid ip")
	}
	loc.IsIPv6 = parsed.To4() == nil
	loc.IsLocal = isPrivateOrLoopback(parsed)
	if loc.IsLocal {
		loc.Country = "Local"
		loc.Display = "Local Network"
		return loc, nil
	}

	r.mu.RLock()
	hasDB := r.hasDB
	searcher := r.searcher
	r.mu.RUnlock()

	if !hasDB {
		loc.Country = "Unknown"
		loc.Display = "Unknown"
		return loc, nil
	}

	region, err := searcher.Search(ip)
	if err != nil {
		loc.Country = "Unknown"
		loc.Display = "Unknown"
		return loc, nil
	}
	parts := strings.Split(region, "|")
	for i, v := range parts {
		if v == "0" {
			parts[i] = ""
		}
	}
	for len(parts) < 5 {
		parts = append(parts, "")
	}
	loc.Country = parts[0]
	loc.Region = parts[2]
	loc.Province = parts[2]
	loc.City = parts[3]
	loc.ISP = parts[4]

	displayParts := make([]string, 0, 4)
	for _, v := range []string{loc.Country, loc.Region, loc.City, loc.ISP} {
		if v != "" && v != "Unknown" {
			displayParts = append(displayParts, v)
		}
	}
	if len(displayParts) == 0 {
		loc.Display = "Unknown"
	} else {
		loc.Display = strings.Join(displayParts, " · ")
	}
	return loc, nil
}

// Close releases internal resources held by the resolver.
func (r *Resolver) Close() error {
	r.mu.Lock()
	defer r.mu.Unlock()
	if r.searcher != nil {
		r.searcher.Close()
		r.searcher = nil
		r.hasDB = false
	}
	return nil
}

// HasDatabase reports whether a geoip database is currently loaded.
func (r *Resolver) HasDatabase() bool {
	r.mu.RLock()
	defer r.mu.RUnlock()
	return r.hasDB
}

func isPrivateOrLoopback(ip net.IP) bool {
	if ip.IsLoopback() || ip.IsPrivate() || ip.IsLinkLocalUnicast() ||
		ip.IsLinkLocalMulticast() || ip.IsUnspecified() {
		return true
	}
	return false
}
