"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { api, type Room } from "@/lib/api";

const LOADERS = ["", "forge", "fabric", "neoforge"];

export default function RoomsPage() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [loader, setLoader] = useState("");
  const [search, setSearch] = useState("");

  useEffect(() => {
    setLoading(true);
    api
      .listRooms({ loader, q: search, limit: 50 })
      .then((res) => {
        setRooms(res.items || []);
        setTotal(res.total);
        setError("");
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  }, [loader, search]);

  const isEmpty = !loading && rooms.length === 0 && !error;

  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <p className="label">Rooms</p>
        <h1 className="display-mono text-4xl font-light">Open lobbies, right now.</h1>
        <p className="max-w-2xl text-sm text-muted">
          Public rooms keep their <span className="font-mono">XXXXX-XXXXX</span>{" "}
          code visible so you can paste it into a friend's client. Hidden rooms
          are not listed; ask the host for the code.
        </p>
      </header>

      <Filters loader={loader} setLoader={setLoader} search={search} setSearch={setSearch} />

      {error && (
        <p className="rounded-md border border-rose-400/40 bg-rose-400/10 p-4 text-sm text-rose-200">
          {error}
        </p>
      )}

      {isEmpty && (
        <div className="rounded-md border border-dashed border-hairline-strong bg-canvas/80 p-10 text-center text-sm text-subtle">
          <p className="display-mono mb-3 text-xl text-ink">$ no rooms found</p>
          <p>
            No public rooms match those filters. Spin one up from the mod or
            relax the filter set.
          </p>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        {rooms.map((r) => (
          <RoomCard key={r.code} room={r} />
        ))}
      </div>

      {total > rooms.length && (
        <p className="text-center text-xs text-subtle">
          Showing {rooms.length} of {total} rooms.
        </p>
      )}
    </div>
  );
}

function Filters({
  loader,
  setLoader,
  search,
  setSearch
}: {
  loader: string;
  setLoader: (v: string) => void;
  search: string;
  setSearch: (v: string) => void;
}) {
  return (
    <div className="flex flex-col gap-3 rounded-md border border-hairline bg-canvas/80 p-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex flex-wrap items-center gap-1">
        {LOADERS.map((l) => {
          const label = l ? l : "all";
          const active = l === loader;
          return (
            <button
              type="button"
              key={label}
              onClick={() => setLoader(l)}
              className={`pill-mono ${active ? "bg-white text-canvas" : "border-hairline-strong text-muted hover:bg-surface-hover"}`}
            >
              {label}
            </button>
          );
        })}
      </div>
      <input
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="search name / motd"
        className="rounded-pill border border-hairline-strong bg-canvas px-4 py-2 font-mono text-xs text-ink outline-none placeholder:text-subtle focus:border-white"
      />
    </div>
  );
}

function RoomCard({ room }: { room: Room }) {
  const fill = useMemo(() => {
    if (!room.maxPlayers) return 0;
    return Math.min(1, room.onlinePlayer / room.maxPlayers);
  }, [room.maxPlayers, room.onlinePlayer]);
  return (
    <Link
      href={`/rooms/view?code=${room.code}`}
      className="card flex h-full flex-col gap-4 transition-colors hover:bg-surface-hover"
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="display-mono text-2xl font-light text-ink">{room.name}</p>
          <p className="font-mono text-xs uppercase tracking-button text-subtle">
            {room.code}
          </p>
        </div>
        {room.hasPassword && (
          <span className="pill-mono border-amber-400/40 text-amber-200">locked</span>
        )}
      </div>
      <p className="text-sm text-muted line-clamp-2">{room.motd || "—"}</p>
      <div className="flex flex-wrap gap-1 text-[10px]">
        <span className="pill-mono border-hairline-strong text-muted">
          {room.loader}
        </span>
        <span className="pill-mono border-hairline-strong text-muted">
          {room.version}
        </span>
        {room.modes.map((m) => (
          <span key={m} className="pill-mono border-hairline-strong text-muted">
            {m}
          </span>
        ))}
        {room.region && (
          <span className="pill-mono border-hairline-strong text-muted">
            {room.region}
          </span>
        )}
      </div>
      <footer className="mt-auto space-y-2 border-t border-hairline pt-3">
        <div className="flex items-center justify-between font-mono text-xs text-subtle">
          <span>{room.onlinePlayer}/{room.maxPlayers || "∞"} online</span>
          <span>{room.natType || "nat: ?"}</span>
        </div>
        <div className="h-1 overflow-hidden rounded-pill bg-surface-elevated">
          <div className="h-full bg-white" style={{ width: `${Math.round(fill * 100)}%` }} />
        </div>
      </footer>
    </Link>
  );
}
