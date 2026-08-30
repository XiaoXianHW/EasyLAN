package nat

import "testing"

func TestClassifyIPv6(t *testing.T) {
	r := Classify("[2001:db8::1]:50000", Probe{HasIPv6: true, UDPSupported: true})
	if r.Type != TypeIPv6 {
		t.Fatalf("expected ipv6, got %v", r.Type)
	}
	if !r.IsIPv6 {
		t.Fatalf("expected IsIPv6=true")
	}
}

func TestClassifyUDPBlocked(t *testing.T) {
	r := Classify("203.0.113.5:51000", Probe{HasIPv6: false, UDPSupported: false})
	if r.Type != TypeUDPBlocked {
		t.Fatalf("expected udp-blocked, got %v", r.Type)
	}
}

func TestClassifySymmetric(t *testing.T) {
	r := Classify("203.0.113.5:51000", Probe{
		ClientObservedIP:   "203.0.113.5",
		ClientObservedPort: 51234,
		UDPSupported:       true,
	})
	if r.Type != TypeSymmetric {
		t.Fatalf("expected symmetric, got %v", r.Type)
	}
	if !r.IsSymmetric {
		t.Fatalf("expected IsSymmetric=true")
	}
}

func TestClassifyFullCone(t *testing.T) {
	r := Classify("203.0.113.5:51000", Probe{
		ClientObservedIP:   "203.0.113.5",
		ClientObservedPort: 51000,
		UDPSupported:       true,
		WebRTCNATType:      "srflx",
	})
	if r.Type != TypeFullCone {
		t.Fatalf("expected full-cone, got %v", r.Type)
	}
}

func TestClassifyOpenInternet(t *testing.T) {
	r := Classify("203.0.113.5:51000", Probe{
		ClientObservedIP:   "203.0.113.5",
		ClientObservedPort: 51000,
		UDPSupported:       true,
		WebRTCNATType:      "host",
	})
	if r.Type != TypeOpenInternet {
		t.Fatalf("expected open-internet, got %v", r.Type)
	}
}
