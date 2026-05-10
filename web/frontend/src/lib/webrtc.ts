// Browser-side helpers for NAT type detection.  We open a single
// RTCPeerConnection, gather ICE candidates against the supplied STUN
// servers, then summarise the discovery into a small struct that we can
// post to /api/nat/probe.

export type WebRTCNATSummary = {
  candidates: number;
  hasIPv6: boolean;
  hasUDPHost: boolean;
  publicIPv4?: string;
  publicPort?: number;
  webrtcType: "host" | "srflx" | "prflx" | "relay" | "unknown";
  raw: RTCIceCandidate[];
};

const ipv4Regex = /(\d{1,3}\.){3}\d{1,3}/;

export async function detectNAT(stunUrls: string[]): Promise<WebRTCNATSummary> {
  const pc = new RTCPeerConnection({
    iceServers: stunUrls.length ? [{ urls: stunUrls }] : []
  });

  const summary: WebRTCNATSummary = {
    candidates: 0,
    hasIPv6: false,
    hasUDPHost: false,
    webrtcType: "unknown",
    raw: []
  };

  return new Promise<WebRTCNATSummary>(async (resolve) => {
    const seen = new Set<string>();
    const finalize = () => {
      try {
        pc.close();
      } catch {
        /* ignore */
      }
      resolve(summary);
    };

    const timer = window.setTimeout(finalize, 4000);

    pc.onicecandidate = (ev) => {
      if (!ev.candidate) {
        clearTimeout(timer);
        finalize();
        return;
      }
      const c = ev.candidate;
      if (!c.candidate) return;
      const key = c.candidate;
      if (seen.has(key)) return;
      seen.add(key);
      summary.candidates += 1;
      summary.raw.push(c);

      const proto = (c.protocol || "udp").toLowerCase();
      const typ = (c.type || "").toLowerCase();
      const address = c.address ?? extractAddress(c.candidate);
      const port = c.port ?? extractPort(c.candidate);

      if (address && address.includes(":")) summary.hasIPv6 = true;
      if (typ === "host" && proto === "udp") summary.hasUDPHost = true;

      if (typ === "srflx" || typ === "prflx") {
        if (address && ipv4Regex.test(address)) {
          summary.publicIPv4 = address;
          summary.publicPort = port ?? undefined;
        }
      }

      // Track strongest signal we've seen.
      const rank = (t: string) => {
        switch (t) {
          case "host":
            return 1;
          case "srflx":
            return 3;
          case "prflx":
            return 2;
          case "relay":
            return 4;
          default:
            return 0;
        }
      };
      if (rank(typ) > rank(summary.webrtcType)) {
        summary.webrtcType = (typ as WebRTCNATSummary["webrtcType"]) || "unknown";
      }
    };

    try {
      pc.createDataChannel("nat-probe");
      const offer = await pc.createOffer({ offerToReceiveAudio: false });
      await pc.setLocalDescription(offer);
    } catch {
      clearTimeout(timer);
      finalize();
    }
  });
}

function extractAddress(candidate: string): string | undefined {
  const parts = candidate.split(" ");
  return parts[4];
}

function extractPort(candidate: string): number | undefined {
  const parts = candidate.split(" ");
  const v = Number(parts[5]);
  return Number.isFinite(v) ? v : undefined;
}
