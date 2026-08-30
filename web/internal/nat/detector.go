// Package nat provides the server-side helpers needed for client NAT
// type detection.  The actual NAT classification happens in the browser:
// the frontend opens a STUN binding to the server, learns its reflexive
// address, then asks the server to verify what address it observed.
//
// This package intentionally stays small — the full RFC 5780 behaviour
// discovery flow requires the server to listen on two distinct IPs,
// which is environment-specific and out of scope for the MVP.
package nat

import (
	"net"
	"strings"
)

// Type names mirror the classic STUN classification.
const (
	TypeUnknown                  = "unknown"
	TypeOpenInternet             = "open-internet"
	TypeFullCone                 = "full-cone"
	TypeRestrictedCone           = "restricted-cone"
	TypePortRestrictedCone       = "port-restricted-cone"
	TypeSymmetric                = "symmetric"
	TypeUDPBlocked               = "udp-blocked"
	TypeSymmetricUDPFirewall     = "symmetric-udp-firewall"
	TypeBlocked                  = "blocked"
	TypeIPv6                     = "ipv6"
	TypeBehindCarrierGradeNAT    = "carrier-grade-nat"
	TypeFullDuplex               = "full-duplex"
	TypeRecommendedForP2P        = "p2p-friendly"
	TypeRecommendedForRelay      = "needs-relay"
	TypeRecommendedForFallback   = "needs-fallback"
	TypeRecommendedForFRP        = "needs-frp"
	DescriptionForRoomP2P        = "Direct UDP hole punching should work."
	DescriptionForRoomRestricted = "Hole punching may need handshake retries."
	DescriptionForRoomSymmetric  = "Direct P2P unlikely; FRP / TURN recommended."
	DescriptionForRoomBlocked    = "UDP appears blocked; FRP / TURN required."
	DescriptionForRoomIPv6       = "Public IPv6 detected; direct connect supported."
	DescriptionForRoomOpen       = "Public IP detected; direct connect supported."
)

// Probe is the request payload posted to the NAT verification endpoint.
type Probe struct {
	ClientObservedIP   string `json:"clientObservedIp"`
	ClientObservedPort int    `json:"clientObservedPort"`
	HasIPv6            bool   `json:"hasIpv6"`
	UDPSupported       bool   `json:"udpSupported"`
	WebRTCNATType      string `json:"webrtcNatType"`
}

// Result is the server-side classification returned to the frontend.
type Result struct {
	Type            string `json:"type"`
	ClientIP        string `json:"clientIp"`
	ClientPort      int    `json:"clientPort"`
	ServerObservedIP string `json:"serverObservedIp"`
	ServerObservedPort int  `json:"serverObservedPort"`
	IsSymmetric     bool   `json:"isSymmetric"`
	IsIPv6          bool   `json:"isIpv6"`
	Description     string `json:"description"`
	Recommendation  string `json:"recommendation"`
}

// Classify combines the server-observed source address with the optional
// client-supplied probe data and produces a best-effort classification.
//
// remoteAddr is the address the request was received from (HTTP).
func Classify(remoteAddr string, probe Probe) Result {
	host, port := splitAddr(remoteAddr)
	res := Result{
		ClientIP:           probe.ClientObservedIP,
		ClientPort:         probe.ClientObservedPort,
		ServerObservedIP:   host,
		ServerObservedPort: port,
	}
	if res.ClientIP == "" {
		res.ClientIP = host
	}
	res.IsIPv6 = strings.Contains(res.ServerObservedIP, ":") || strings.Contains(res.ClientIP, ":")
	if res.IsIPv6 {
		res.Type = TypeIPv6
		res.Description = DescriptionForRoomIPv6
		res.Recommendation = TypeRecommendedForP2P
		return res
	}
	if !probe.UDPSupported {
		res.Type = TypeUDPBlocked
		res.Description = DescriptionForRoomBlocked
		res.Recommendation = TypeRecommendedForFRP
		return res
	}

	if probe.ClientObservedIP != "" && probe.ClientObservedPort != 0 {
		if probe.ClientObservedIP != host {
			res.Type = TypeSymmetric
			res.IsSymmetric = true
			res.Description = DescriptionForRoomSymmetric
			res.Recommendation = TypeRecommendedForFRP
			return res
		}
		if probe.ClientObservedPort != port && port != 0 {
			res.Type = TypeSymmetric
			res.IsSymmetric = true
			res.Description = DescriptionForRoomSymmetric
			res.Recommendation = TypeRecommendedForFRP
			return res
		}
	}

	switch probe.WebRTCNATType {
	case "host":
		res.Type = TypeOpenInternet
		res.Description = DescriptionForRoomOpen
		res.Recommendation = TypeRecommendedForP2P
	case "srflx":
		res.Type = TypeFullCone
		res.Description = DescriptionForRoomP2P
		res.Recommendation = TypeRecommendedForP2P
	case "prflx":
		res.Type = TypeRestrictedCone
		res.Description = DescriptionForRoomRestricted
		res.Recommendation = TypeRecommendedForP2P
	case "relay":
		res.Type = TypeSymmetric
		res.IsSymmetric = true
		res.Description = DescriptionForRoomSymmetric
		res.Recommendation = TypeRecommendedForRelay
	default:
		res.Type = TypeUnknown
		res.Description = "Unable to determine NAT type from supplied probe."
		res.Recommendation = TypeRecommendedForFallback
	}
	return res
}

func splitAddr(addr string) (host string, port int) {
	if addr == "" {
		return "", 0
	}
	h, p, err := net.SplitHostPort(addr)
	if err != nil {
		return addr, 0
	}
	host = h
	if p != "" {
		// port string -> int; ignore parse failure (returns 0).
		var v int
		for i := 0; i < len(p); i++ {
			c := p[i]
			if c < '0' || c > '9' {
				return host, 0
			}
			v = v*10 + int(c-'0')
		}
		port = v
	}
	return host, port
}
