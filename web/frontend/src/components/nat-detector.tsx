"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { api, type IPInfo, type NATResult } from "@/lib/api";
import { detectNAT, type WebRTCNATSummary } from "@/lib/webrtc";

type DetectionState =
  | { phase: "idle" }
  | { phase: "loading-ip" }
  | { phase: "loading-nat"; ip: IPInfo }
  | { phase: "ready"; ip: IPInfo; nat: NATResult; rtc: WebRTCNATSummary }
  | { phase: "error"; error: string };

const NAT_LABELS: Record<string, { label: string; tone: string }> = {
  "open-internet": { label: "Open Internet", tone: "border-emerald-400/40 text-emerald-200" },
  "ipv6": { label: "Public IPv6", tone: "border-emerald-400/40 text-emerald-200" },
  "full-cone": { label: "Full Cone NAT", tone: "border-emerald-400/40 text-emerald-200" },
  "restricted-cone": { label: "Restricted Cone NAT", tone: "border-amber-400/40 text-amber-200" },
  "port-restricted-cone": { label: "Port-Restricted Cone NAT", tone: "border-amber-400/40 text-amber-200" },
  "symmetric": { label: "Symmetric NAT", tone: "border-rose-400/40 text-rose-200" },
  "udp-blocked": { label: "UDP Blocked", tone: "border-rose-400/40 text-rose-200" },
  "blocked": { label: "Blocked", tone: "border-rose-400/40 text-rose-200" },
  "unknown": { label: "Unknown", tone: "border-white/20 text-muted" }
};

export function NATDetector() {
  const [state, setState] = useState<DetectionState>({ phase: "idle" });

  const run = useCallback(async () => {
    setState({ phase: "loading-ip" });
    try {
      const ip = await api.ip();
      setState({ phase: "loading-nat", ip });
      const rtc = await detectNAT(ip.stunServers);
      const probe = {
        clientObservedIp: rtc.publicIPv4 ?? ip.ip,
        clientObservedPort: rtc.publicPort ?? 0,
        hasIpv6: rtc.hasIPv6 || ip.ipv6,
        udpSupported: rtc.hasUDPHost || rtc.candidates > 0,
        webrtcNatType: rtc.webrtcType
      };
      const nat = await api.natProbe(probe);
      setState({ phase: "ready", ip, nat, rtc });
    } catch (err) {
      setState({ phase: "error", error: (err as Error).message });
    }
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (typeof window.RTCPeerConnection === "undefined") {
      setState({ phase: "error", error: "WebRTC is not available in this browser." });
      return;
    }
    void run();
  }, [run]);

  return (
    <section className="card relative overflow-hidden">
      <Header phase={state.phase} onRetry={run} />
      <div className="mt-6 grid gap-6 md:grid-cols-2">
        <NetworkPanel state={state} />
        <NATPanel state={state} />
      </div>
      <CandidateList state={state} />
    </section>
  );
}

function Header({ phase, onRetry }: { phase: DetectionState["phase"]; onRetry: () => void }) {
  return (
    <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p className="label">Network probe</p>
        <h2 className="display-mono mt-1 text-3xl font-light">$ easylan probe</h2>
      </div>
      <div className="flex items-center gap-3 text-xs text-subtle font-mono uppercase tracking-button">
        <PhaseTag phase={phase} />
        <button
          type="button"
          onClick={onRetry}
          className="pill-mono border-hairline-strong text-muted hover:bg-surface-hover"
        >
          Re-run
        </button>
      </div>
    </div>
  );
}

function PhaseTag({ phase }: { phase: DetectionState["phase"] }) {
  const tag = useMemo(() => {
    switch (phase) {
      case "loading-ip":
        return { label: "Resolving IP…", tone: "text-amber-200" };
      case "loading-nat":
        return { label: "Probing NAT…", tone: "text-amber-200" };
      case "ready":
        return { label: "OK", tone: "text-emerald-300" };
      case "error":
        return { label: "Error", tone: "text-rose-300" };
      case "idle":
      default:
        return { label: "Idle", tone: "text-subtle" };
    }
  }, [phase]);
  return <span className={`pill-mono border-transparent ${tag.tone}`}>{tag.label}</span>;
}

function NetworkPanel({ state }: { state: DetectionState }) {
  if (state.phase === "loading-ip" || state.phase === "idle") {
    return <Skeleton title="Network" />;
  }
  if (state.phase === "error") {
    return (
      <div className="rounded-md border border-rose-400/40 bg-rose-400/10 p-4 text-sm text-rose-200">
        {state.error}
      </div>
    );
  }
  const ip =
    state.phase === "ready" || state.phase === "loading-nat" ? state.ip : null;
  if (!ip) return null;
  return (
    <div className="rounded-md border border-hairline bg-canvas/80 p-4 text-sm font-mono">
      <div className="label mb-3">Network</div>
      <Row label="ip" value={ip.ip || "?"} />
      <Row label="proto" value={ip.ipv6 ? "IPv6" : "IPv4"} />
      <Row label="country" value={ip.country || "—"} />
      <Row label="region" value={[ip.province, ip.city].filter(Boolean).join(" · ") || "—"} />
      <Row label="isp" value={ip.isp || "—"} />
      <Row label="display" value={ip.display || "—"} />
      <Row label="local" value={ip.isLocal ? "yes" : "no"} />
    </div>
  );
}

function NATPanel({ state }: { state: DetectionState }) {
  if (state.phase === "loading-ip" || state.phase === "loading-nat" || state.phase === "idle") {
    return <Skeleton title="NAT" />;
  }
  if (state.phase !== "ready") return null;
  const meta = NAT_LABELS[state.nat.type] || NAT_LABELS.unknown;
  return (
    <div className="rounded-md border border-hairline bg-canvas/80 p-4 text-sm font-mono">
      <div className="mb-3 flex items-center justify-between">
        <span className="label">NAT type</span>
        <span className={`pill-mono ${meta.tone}`}>{meta.label}</span>
      </div>
      <Row label="server-observed" value={`${state.nat.serverObservedIp}:${state.nat.serverObservedPort}`} />
      <Row label="client-observed" value={`${state.nat.clientIp}:${state.nat.clientPort}`} />
      <Row label="symmetric" value={state.nat.isSymmetric ? "yes" : "no"} />
      <Row label="webrtc-type" value={state.rtc.webrtcType} />
      <Row label="ipv6" value={state.rtc.hasIPv6 ? "yes" : "no"} />
      <p className="mt-3 border-t border-hairline pt-3 text-xs text-muted">
        {state.nat.description}
      </p>
      <p className="mt-2 text-xs text-subtle">
        Recommended mode → <span className="text-ink">{state.nat.recommendation}</span>
      </p>
    </div>
  );
}

function CandidateList({ state }: { state: DetectionState }) {
  if (state.phase !== "ready" || state.rtc.raw.length === 0) return null;
  return (
    <details className="mt-6 rounded-md border border-hairline bg-canvas/80 p-4 text-xs font-mono text-muted">
      <summary className="cursor-pointer select-none text-subtle">
        ICE candidates ({state.rtc.raw.length})
      </summary>
      <ul className="mt-3 space-y-1">
        {state.rtc.raw.map((c, i) => (
          <li key={i} className="break-all">
            {c.candidate}
          </li>
        ))}
      </ul>
    </details>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-baseline gap-3 py-1">
      <span className="w-32 shrink-0 text-subtle">{label}</span>
      <span className="break-all text-ink">{value}</span>
    </div>
  );
}

function Skeleton({ title }: { title: string }) {
  return (
    <div className="rounded-md border border-hairline bg-canvas/80 p-4 text-sm font-mono text-subtle">
      <div className="label mb-3">{title}</div>
      <div className="space-y-2">
        <div className="h-3 w-2/3 rounded-pill bg-surface-elevated" />
        <div className="h-3 w-1/2 rounded-pill bg-surface-elevated" />
        <div className="h-3 w-3/4 rounded-pill bg-surface-elevated" />
        <div className="h-3 w-1/3 rounded-pill bg-surface-elevated" />
      </div>
    </div>
  );
}
