"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const links = [
  { href: "/", label: "Home" },
  { href: "/rooms", label: "Rooms" },
  { href: "/download", label: "Download" },
  { href: "/docs", label: "Docs" }
];

export function TopNav() {
  const pathname = usePathname();
  return (
    <header className="border-b border-hairline">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5 sm:px-10">
        <Link href="/" className="flex items-center gap-3">
          <span className="grid h-8 w-8 place-items-center rounded-md border border-hairline-strong font-mono text-xs">
            EL
          </span>
          <span className="font-mono text-sm uppercase tracking-button">EasyLAN</span>
        </Link>
        <nav className="flex items-center gap-1">
          {links.map((l) => {
            const active =
              l.href === "/" ? pathname === "/" : pathname.startsWith(l.href);
            return (
              <Link
                key={l.href}
                href={l.href}
                className={`rounded-pill px-4 py-2 font-mono text-xs uppercase tracking-button transition-colors ${
                  active
                    ? "bg-white text-canvas"
                    : "text-muted hover:bg-surface-hover"
                }`}
              >
                {l.label}
              </Link>
            );
          })}
          <a
            href="https://github.com/XiaoXianHW/EasyLAN"
            target="_blank"
            rel="noreferrer"
            className="ml-2 hidden rounded-pill border border-hairline-strong px-4 py-2 font-mono text-xs uppercase tracking-button text-muted hover:bg-surface-hover sm:inline-flex"
          >
            GitHub
          </a>
        </nav>
      </div>
    </header>
  );
}
