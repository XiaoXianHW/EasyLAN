"use client";

import { useEffect, useState } from "react";
import { api, type DownloadGroup } from "@/lib/api";

const LOADER_LABEL: Record<string, string> = {
  forge: "Forge",
  fabric: "Fabric",
  neoforge: "NeoForge"
};

export default function DownloadPage() {
  const [groups, setGroups] = useState<DownloadGroup[]>([]);
  const [error, setError] = useState<string>("");
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    void api
      .downloads()
      .then((res) => setGroups(res.groups))
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoaded(true));
  }, []);

  return (
    <div className="space-y-10">
      <header className="space-y-3">
        <p className="label">Download</p>
        <h1 className="display-mono text-4xl font-light">Pick your mod loader.</h1>
        <p className="max-w-2xl text-sm text-muted">
          The mod runs on Forge, Fabric and NeoForge. Each row links straight
          to the matching version page on CurseForge or Modrinth — pick
          whichever distribution channel you prefer.
        </p>
      </header>

      {!loaded && <Skeleton />}
      {error && (
        <p className="rounded-md border border-rose-400/40 bg-rose-400/10 p-4 text-sm text-rose-200">
          {error}
        </p>
      )}

      {groups.map((g) => (
        <section key={g.loader} className="space-y-4">
          <div className="flex items-end justify-between">
            <div>
              <p className="label">Loader</p>
              <h2 className="display-mono mt-1 text-2xl font-light">
                {LOADER_LABEL[g.loader] ?? g.loader}
              </h2>
              <p className="mt-1 text-xs text-subtle">
                {g.versions.length} supported version{g.versions.length === 1 ? "" : "s"}
              </p>
            </div>
            <span className="pill-mono border-hairline-strong text-muted">
              branch group
            </span>
          </div>
          <div className="overflow-hidden rounded-md border border-hairline bg-canvas/80">
            <table className="w-full table-fixed text-sm">
              <thead>
                <tr className="border-b border-hairline text-left font-mono uppercase tracking-button text-[10px] text-subtle">
                  <th className="w-1/4 px-4 py-3">Version</th>
                  <th className="w-1/3 px-4 py-3">Branch group</th>
                  <th className="w-1/4 px-4 py-3">Tags</th>
                  <th className="w-1/4 px-4 py-3 text-right">Get the mod</th>
                </tr>
              </thead>
              <tbody>
                {g.versions.map((v) => (
                  <tr
                    key={`${v.loader}-${v.version}`}
                    className="border-b border-hairline/60 transition-colors last:border-b-0 hover:bg-surface-hover"
                  >
                    <td className="px-4 py-3 font-mono text-ink">{v.version}</td>
                    <td className="px-4 py-3 font-mono text-subtle">{v.branchGroup}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-1">
                        {(v.tags || []).map((t) => (
                          <span
                            key={t}
                            className="pill-mono border-hairline-strong text-subtle"
                          >
                            {t}
                          </span>
                        ))}
                        {v.notes && (
                          <span className="pill-mono border-emerald-400/40 text-emerald-200">
                            {v.notes}
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="inline-flex items-center gap-2">
                        <a
                          className="pill-mono border-hairline-strong hover:bg-surface-hover"
                          href={v.curseforge}
                          target="_blank"
                          rel="noreferrer"
                        >
                          CurseForge ↗
                        </a>
                        <a
                          className="pill-mono border-hairline-strong hover:bg-surface-hover"
                          href={v.modrinth}
                          target="_blank"
                          rel="noreferrer"
                        >
                          Modrinth ↗
                        </a>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      ))}
    </div>
  );
}

function Skeleton() {
  return (
    <div className="space-y-4">
      {[0, 1, 2].map((i) => (
        <div
          key={i}
          className="h-40 animate-pulse rounded-md border border-hairline bg-surface-elevated"
        />
      ))}
    </div>
  );
}
