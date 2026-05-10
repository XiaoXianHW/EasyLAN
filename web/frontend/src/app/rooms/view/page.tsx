"use client";

import { Suspense, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { api, type Room } from "@/lib/api";

export default function RoomViewPage() {
  return (
    <Suspense fallback={<p className="font-mono text-sm text-subtle">Loading…</p>}>
      <RoomView />
    </Suspense>
  );
}

function RoomView() {
  const params = useSearchParams();
  const code = (params.get("code") || "").toUpperCase();
  const [room, setRoom] = useState<Room | null>(null);
  const [error, setError] = useState("");
  const [password, setPassword] = useState("");
  const [joinResult, setJoinResult] = useState<Room | null>(null);

  useEffect(() => {
    if (!code) return;
    api
      .getRoom(code)
      .then(setRoom)
      .catch((err: Error) => setError(err.message));
  }, [code]);

  const join = async () => {
    setError("");
    try {
      const data = await api.joinRoom(code, password);
      setJoinResult(data);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  if (!code) {
    return (
      <p className="rounded-md border border-rose-400/40 bg-rose-400/10 p-4 text-sm text-rose-200">
        Missing <code>?code=XXXXX-XXXXX</code> query parameter.
      </p>
    );
  }
  if (!room && !error) {
    return <p className="font-mono text-sm text-subtle">Loading {code}…</p>;
  }
  if (error && !room) {
    return (
      <p className="rounded-md border border-rose-400/40 bg-rose-400/10 p-4 text-sm text-rose-200">
        {error}
      </p>
    );
  }
  if (!room) return null;

  return (
    <div className="space-y-8">
      <Link
        href="/rooms"
        className="font-mono text-xs uppercase tracking-button text-subtle hover:text-ink"
      >
        ← back to rooms
      </Link>
      <header className="space-y-3">
        <span className="pill-mono border-hairline-strong text-muted">{room.code}</span>
        <h1 className="display-mono text-4xl font-light">{room.name}</h1>
        <p className="text-sm text-muted">{room.motd || "—"}</p>
      </header>

      <section className="grid gap-4 md:grid-cols-2">
        <Stat label="loader" value={room.loader} />
        <Stat label="version" value={room.version} />
        <Stat
          label="players"
          value={`${room.onlinePlayer} / ${room.maxPlayers || "∞"}`}
        />
        <Stat label="region" value={room.region || "—"} />
        <Stat label="nat type" value={room.natType || "?"} />
        <Stat label="modes" value={room.modes.join(", ") || "—"} />
      </section>

      <section className="card space-y-4">
        <h2 className="display-mono text-2xl font-light">Join this room</h2>
        {room.hasPassword ? (
          <div className="flex items-center gap-2">
            <input
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              type="password"
              placeholder="room password"
              className="flex-1 rounded-pill border border-hairline-strong bg-canvas px-4 py-2 font-mono text-xs text-ink outline-none placeholder:text-subtle focus:border-white"
            />
            <button
              type="button"
              onClick={join}
              className="pill-primary font-mono uppercase tracking-button text-xs"
            >
              Join
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={join}
            className="pill-primary font-mono uppercase tracking-button text-xs"
          >
            Get connection info
          </button>
        )}
        {error && (
          <p className="rounded-md border border-rose-400/40 bg-rose-400/10 p-3 text-sm text-rose-200">
            {error}
          </p>
        )}
        {joinResult && (
          <pre className="overflow-x-auto rounded-md border border-hairline bg-canvas p-4 font-mono text-xs leading-relaxed text-muted">
{JSON.stringify(joinResult, null, 2)}
          </pre>
        )}
      </section>

      <section className="card space-y-3">
        <h2 className="display-mono text-2xl font-light">
          Players ({room.players.length})
        </h2>
        {room.players.length === 0 ? (
          <p className="text-sm text-subtle">No players reported by the host.</p>
        ) : (
          <ul className="grid gap-1 text-sm font-mono text-muted sm:grid-cols-2">
            {room.players.map((p) => (
              <li key={p}>· {p}</li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border border-hairline bg-canvas/80 p-4">
      <p className="label">{label}</p>
      <p className="display-mono mt-2 text-xl text-ink">{value}</p>
    </div>
  );
}
