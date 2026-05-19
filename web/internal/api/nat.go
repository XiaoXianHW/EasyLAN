package api

import (
	"net/http"
	"time"

	"github.com/xiaoxianhw/easylan/web/internal/nat"
)

// IPInfo bundles GeoIP and STUN-server metadata returned to the
// frontend.  The frontend uses STUNServers to bootstrap its WebRTC NAT
// detection.
type IPInfo struct {
	IP          string   `json:"ip"`
	IPv6        bool     `json:"ipv6"`
	Country     string   `json:"country"`
	Region      string   `json:"region"`
	Province    string   `json:"province"`
	City        string   `json:"city"`
	ISP         string   `json:"isp"`
	Display     string   `json:"display"`
	IsLocal     bool     `json:"isLocal"`
	STUNServers []string `json:"stunServers"`
	ServerTime  string   `json:"serverTime"`
}

// IP returns geolocated information for the calling client.  This is
// the data shown on the homepage hero card.
func (h *Handler) IP(w http.ResponseWriter, r *http.Request) {
	ip := clientIP(r)
	loc, err := h.deps.GeoIP.Lookup(ip)
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid_ip", err.Error())
		return
	}
	resp := IPInfo{
		IP:         loc.IP,
		IPv6:       loc.IsIPv6,
		Country:    loc.Country,
		Region:     loc.Region,
		Province:   loc.Province,
		City:       loc.City,
		ISP:        loc.ISP,
		Display:    loc.Display,
		IsLocal:    loc.IsLocal,
		STUNServers: h.publicStunServers(),
		ServerTime:  time.Now().UTC().Format(time.RFC3339),
	}
	writeJSON(w, http.StatusOK, resp)
}

// NATProbeRequest is the payload posted by the frontend after it has
// finished its WebRTC ICE candidate gathering.
type NATProbeRequest struct {
	ClientObservedIP   string `json:"clientObservedIp"`
	ClientObservedPort int    `json:"clientObservedPort"`
	HasIPv6            bool   `json:"hasIpv6"`
	UDPSupported       bool   `json:"udpSupported"`
	WebRTCNATType      string `json:"webrtcNatType"`
}

// NATProbe combines the client probe data with server-side observation
// to classify the caller's NAT type.
func (h *Handler) NATProbe(w http.ResponseWriter, r *http.Request) {
	var req NATProbeRequest
	if r.ContentLength > 0 {
		if err := decodeJSON(r, &req); err != nil {
			writeError(w, http.StatusBadRequest, "bad_request", err.Error())
			return
		}
	}
	probe := nat.Probe{
		ClientObservedIP:   req.ClientObservedIP,
		ClientObservedPort: req.ClientObservedPort,
		HasIPv6:            req.HasIPv6,
		UDPSupported:       req.UDPSupported,
		WebRTCNATType:      req.WebRTCNATType,
	}
	res := nat.Classify(r.RemoteAddr, probe)
	writeJSON(w, http.StatusOK, res)
}

// publicStunServers returns the stun:// URLs the frontend should use.
//
// We include our own STUN listener (when EASYLAN_STUN_PUBLIC is set so
// it has a real host) and a couple of well-known public servers so the
// WebRTC detection works even when our STUN port isn't reachable.
func (h *Handler) publicStunServers() []string {
	out := []string{}
	if h.deps.StunPublic != "" {
		out = append(out, "stun:"+h.deps.StunPublic)
	}
	out = append(out,
		"stun:stun.l.google.com:19302",
		"stun:stun1.l.google.com:19302",
		"stun:stun.cloudflare.com:3478",
	)
	return out
}
