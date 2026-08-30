import Link from "next/link";

export default function DocsPage() {
  return (
    <div className="grid gap-10 lg:grid-cols-[220px_1fr]">
      <aside className="hidden lg:block">
        <nav className="sticky top-24 space-y-2 text-xs font-mono uppercase tracking-button">
          <a href="#getting-started" className="block text-muted hover:text-ink">
            01 · Getting started
          </a>
          <a href="#room-codes" className="block text-muted hover:text-ink">
            02 · Room codes
          </a>
          <a href="#nat-detection" className="block text-muted hover:text-ink">
            03 · NAT detection
          </a>
          <a href="#api" className="block text-muted hover:text-ink">
            04 · HTTP API
          </a>
          <a href="#deploy" className="block text-muted hover:text-ink">
            05 · Self-hosting
          </a>
        </nav>
      </aside>

      <article className="prose-easylan max-w-3xl space-y-12 text-sm leading-relaxed text-muted">
        <header className="space-y-3">
          <p className="label">Documentation</p>
          <h1 className="display-mono text-4xl font-light text-ink">How EasyLAN works.</h1>
          <p>
            EasyLAN ships as a Minecraft mod plus a single Go binary that runs
            the room-matching platform. Both halves talk a tiny JSON protocol;
            this page is the source-of-truth reference.
          </p>
        </header>

        <Section id="getting-started" title="01 · Getting started">
          <ol className="list-decimal space-y-2 pl-5">
            <li>
              Install the mod from{" "}
              <Link href="/download" className="text-ink underline">
                /download
              </Link>{" "}
              into your Minecraft instance.
            </li>
            <li>
              Open a single-player world and use <code>/easylan open</code> to
              create a room. The mod prints a <code>XXXXX-XXXXX</code> code in
              chat.
            </li>
            <li>
              Share that code with your friends. They paste it into their own
              clients to join.
            </li>
          </ol>
        </Section>

        <Section id="room-codes" title="02 · Room codes">
          <p>
            Codes are random 10-character strings drawn from a 31-character
            alphabet (no <code>0/O/I/L/1</code>) and split as{" "}
            <code>XXXXX-XXXXX</code> for legibility. We never reuse a code that
            still maps to an active room — collisions are detected at create
            time and the mod retries.
          </p>
          <p>
            A room's metadata mirrors the EasyLAN mod's <code>/status</code>{" "}
            HTTP API. The mod publishes <code>name</code>, <code>version</code>,{" "}
            <code>motd</code>, <code>maxPlayer</code>, <code>onlinePlayer</code>,
            and the platform layers on top: connection modes, IP-derived region,
            NAT type, and a TTL that the mod refreshes via heartbeat.
          </p>
        </Section>

        <Section id="nat-detection" title="03 · NAT detection">
          <p>
            We use a hybrid approach: a STUN UDP server runs alongside the API,
            and the browser performs WebRTC ICE candidate gathering. The
            results are combined server-side to classify each peer as one of:{" "}
            <code>open-internet</code>, <code>full-cone</code>,{" "}
            <code>restricted-cone</code>, <code>port-restricted-cone</code>,{" "}
            <code>symmetric</code>, <code>udp-blocked</code>, <code>ipv6</code>.
          </p>
          <p>
            That classification picks the connectivity mode the room defaults
            to: cone-typed peers prefer P2P hole punching, symmetric peers fall
            back to FRP, blocked peers are routed through a TURN relay.
          </p>
        </Section>

        <Section id="api" title="04 · HTTP API">
          <Endpoint
            method="GET"
            path="/api/ip"
            blurb="Returns the caller's IP, GeoIP-resolved region, and the STUN servers the frontend should use."
          />
          <Endpoint
            method="POST"
            path="/api/nat/probe"
            blurb="Body: { clientObservedIp, clientObservedPort, hasIpv6, udpSupported, webrtcNatType }. Returns the classification result."
          />
          <Endpoint
            method="POST"
            path="/api/rooms"
            blurb="Create a room. Returns the code and a private ownerToken used for heartbeats / deletes."
          />
          <Endpoint
            method="GET"
            path="/api/rooms"
            blurb="List public rooms. Supports loader, version, region, q (search), open, limit, offset query params."
          />
          <Endpoint
            method="GET"
            path="/api/rooms/{code}"
            blurb="Fetch a single room's public metadata."
          />
          <Endpoint
            method="POST"
            path="/api/rooms/{code}/join"
            blurb="Validate the password and return connection metadata."
          />
          <Endpoint
            method="POST"
            path="/api/rooms/{code}/heartbeat"
            blurb="Owner-only: refresh expiry and replace player list / NAT type."
          />
          <Endpoint
            method="DELETE"
            path="/api/rooms/{code}"
            blurb="Owner-only (X-Owner-Token header). Removes the room."
          />
          <Endpoint
            method="POST"
            path="/api/mod/status"
            blurb="Admin/debug — body: { baseUrl }. Proxies a GET /status + /playerlist against the EasyLAN mod's HTTP API and returns the parsed snapshot."
          />
        </Section>

        <Section id="deploy" title="05 · Self-hosting">
          <p>
            All configuration is environment-driven; sensible defaults apply
            so a bare <code>./easylan-web</code> spawns an HTTP server on{" "}
            <code>:8080</code>, a STUN server on <code>:3478</code>, an SQLite
            database at <code>easylan.db</code>, and an in-memory cache.
          </p>
          <pre className="overflow-x-auto rounded-md border border-hairline bg-canvas p-4 font-mono text-xs leading-relaxed text-muted">
{`EASYLAN_LISTEN=":8080"
EASYLAN_STUN_LISTEN=":3478"
EASYLAN_STORAGE_DRIVER=sqlite      # sqlite | mysql | memory
EASYLAN_STORAGE_DSN=easylan.db
EASYLAN_CACHE_DRIVER=memory        # memory | redis | none
EASYLAN_CACHE_URL=                 # redis://localhost:6379/0
EASYLAN_GEOIP_PATH=                # path to ip2region.xdb
EASYLAN_PUBLIC_BASE_URL=
EASYLAN_ROOM_TTL_SECONDS=900
EASYLAN_LOG_LEVEL=info`}
          </pre>
          <p>
            For production, point <code>EASYLAN_STORAGE_DRIVER=mysql</code> at
            a MySQL 5.7+ DSN and <code>EASYLAN_CACHE_DRIVER=redis</code> at any
            reachable Redis. The bundled <code>docker-compose.yml</code> spins
            both up locally for testing.
          </p>
        </Section>
      </article>
    </div>
  );
}

function Section({
  id,
  title,
  children
}: {
  id: string;
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section id={id} className="space-y-4 scroll-mt-24">
      <h2 className="display-mono text-2xl font-light text-ink">{title}</h2>
      <div className="space-y-3 text-muted">{children}</div>
    </section>
  );
}

function Endpoint({
  method,
  path,
  blurb
}: {
  method: string;
  path: string;
  blurb: string;
}) {
  const tone =
    method === "GET"
      ? "border-emerald-400/40 text-emerald-200"
      : method === "POST"
        ? "border-amber-400/40 text-amber-200"
        : "border-rose-400/40 text-rose-200";
  return (
    <div className="flex flex-col gap-1 rounded-md border border-hairline bg-canvas/80 p-4 sm:flex-row sm:items-start sm:gap-4">
      <span className={`pill-mono ${tone}`}>{method}</span>
      <div className="space-y-1">
        <code className="text-sm font-mono text-ink">{path}</code>
        <p className="text-xs text-subtle">{blurb}</p>
      </div>
    </div>
  );
}
