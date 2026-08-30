import Link from "next/link";
import { NATDetector } from "@/components/nat-detector";

const FEATURES: { title: string; body: string }[] = [
  {
    title: "Room codes",
    body:
      "Every room is identified by a XXXXX-XXXXX code that mirrors the EasyLAN mod's HTTP API: name, version, MOTD, players, max players. Share the code, jump in."
  },
  {
    title: "NAT-aware",
    body:
      "We classify each player's NAT type via WebRTC + STUN before pairing them up, so the platform can choose between P2P, FRP, IPv6 or relay automatically."
  },
  {
    title: "Multi-loader",
    body:
      "First-class support for Forge, Fabric and NeoForge. The download page auto-routes to CurseForge or Modrinth for any supported MC version."
  },
  {
    title: "One Go binary",
    body:
      "The web platform embeds the Next.js frontend. Run `easylan-web` and you get the API, the STUN server, and the UI from a single ~30 MB executable."
  }
];

export default function HomePage() {
  return (
    <div className="space-y-12">
      <Hero />
      <NATDetector />
      <Features />
      <Modes />
    </div>
  );
}

function Hero() {
  return (
    <section className="grid gap-8 pt-4 md:grid-cols-[1.4fr_1fr] md:items-end">
      <div className="space-y-6">
        <span className="pill-mono border-hairline-strong text-muted">
          MINECRAFT · LAN · P2P · FRP · IPV6
        </span>
        <h1 className="display-mono text-5xl font-light leading-tight sm:text-6xl">
          Local-feel multiplayer,<br />no port forwarding.
        </h1>
        <p className="max-w-xl text-base text-muted">
          EasyLAN turns any Minecraft instance into a shareable room. Drop the
          mod into your modpack, paste the generated <span className="font-mono">XXXXX-XXXXX</span>{" "}
          code into your friends' clients, and the platform negotiates the
          fastest connection it can build.
        </p>
        <div className="flex flex-wrap items-center gap-3 pt-2">
          <Link href="/download" className="pill-primary font-mono uppercase tracking-button text-xs">
            Download mod
          </Link>
          <Link href="/rooms" className="pill-ghost font-mono uppercase tracking-button text-xs">
            Browse rooms
          </Link>
          <Link href="/docs" className="pill-mono border-hairline-strong text-muted hover:bg-surface-hover">
            Read docs
          </Link>
        </div>
      </div>
      <pre className="rounded-md border border-hairline bg-canvas/80 p-5 font-mono text-xs leading-relaxed text-muted">
{`$ ./easylan-web
http: listening :8080
stun: listening :3478
storage: sqlite easylan.db
cache: memory
geoip: ip2region.xdb (loaded)
ready.`}
      </pre>
    </section>
  );
}

function Features() {
  return (
    <section className="space-y-6">
      <header className="flex items-end justify-between">
        <div>
          <p className="label">What it does</p>
          <h2 className="display-mono mt-1 text-3xl font-light">Built for room-code multiplayer.</h2>
        </div>
        <Link
          href="/docs"
          className="pill-mono border-hairline-strong text-muted hover:bg-surface-hover"
        >
          Architecture
        </Link>
      </header>
      <div className="grid gap-4 sm:grid-cols-2">
        {FEATURES.map((f) => (
          <div key={f.title} className="card hover:bg-surface-hover">
            <h3 className="display-mono text-xl font-light">{f.title}</h3>
            <p className="mt-3 text-sm text-muted">{f.body}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

function Modes() {
  const modes: { key: string; title: string; body: string }[] = [
    {
      key: "p2p",
      title: "P2P / hole punch",
      body: "Default for cone NATs. Both peers swap reflexive addresses through our signaling channel and punch UDP through their NATs."
    },
    {
      key: "ipv6",
      title: "IPv6 direct",
      body: "If both ends speak IPv6 we skip the punching dance and connect directly. Almost always the lowest-latency option."
    },
    {
      key: "frp",
      title: "FRP fallback",
      body: "When the host is behind symmetric NAT or UDP is blocked, the room code falls back to a stateless FRP relay."
    },
    {
      key: "relay",
      title: "TURN relay",
      body: "Last-resort relayed traffic for players whose carriers actively interfere with hole punching."
    }
  ];
  return (
    <section className="space-y-6">
      <header>
        <p className="label">Connectivity modes</p>
        <h2 className="display-mono mt-1 text-3xl font-light">Pick whichever path works.</h2>
      </header>
      <div className="grid gap-3 md:grid-cols-2">
        {modes.map((m) => (
          <div
            key={m.key}
            className="rounded-md border border-hairline bg-canvas/80 p-5 transition-colors hover:bg-surface-hover"
          >
            <div className="flex items-center gap-3">
              <span className="pill-mono border-hairline-strong text-subtle">{m.key}</span>
              <h3 className="font-mono text-sm uppercase tracking-button">{m.title}</h3>
            </div>
            <p className="mt-3 text-sm text-muted">{m.body}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
