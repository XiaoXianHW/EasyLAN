export function Footer() {
  return (
    <footer className="mt-16 border-t border-hairline">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-6 py-8 text-xs text-subtle sm:flex-row sm:items-center sm:justify-between sm:px-10">
        <div className="flex items-center gap-3 font-mono uppercase tracking-button">
          <span className="grid h-6 w-6 place-items-center rounded-md border border-hairline-strong text-[10px]">
            EL
          </span>
          <span>EasyLAN — single-binary LAN room platform</span>
        </div>
        <div className="flex items-center gap-4 font-mono uppercase tracking-button">
          <a href="https://github.com/XiaoXianHW/EasyLAN" target="_blank" rel="noreferrer">
            GitHub
          </a>
          <a
            href="https://www.curseforge.com/minecraft/mc-mods/easylan"
            target="_blank"
            rel="noreferrer"
          >
            CurseForge
          </a>
          <a href="https://modrinth.com/mod/easylan" target="_blank" rel="noreferrer">
            Modrinth
          </a>
        </div>
      </div>
    </footer>
  );
}
