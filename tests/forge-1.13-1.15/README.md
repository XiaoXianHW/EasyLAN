# Forge 1.13.2-1.15.2 LAN regression

Scope: the failed forge-1.13.2 and forge-1.14.2 rows of the EasyLAN v1.6 manual retest.

- 1.13.2 port input: `charTyped` cleared the field whenever the partial value was out of range, so typing `3` for `31234` emptied it. Typing no longer rewrites the field; the start button and warning text reject invalid final values.
- Max players stuck at 8: the bridge only looked up `getPlayerList`, which is `func_184103_al` in reobfuscated jars. The production name and a field-type fallback are now included.
- Custom port not bound: EasyLAN opened a second endpoint and then ran the vanilla button, which published a random port. `/status` echoed the input text. The start button now calls `IntegratedServer.shareToLAN` once with the requested (or an available) port, and chat/HTTP report `getServerPort()`.

The same code is shared by 1.14.4 and 1.15.2, so those versions get the same fix.

## Run focused tests

From the repository root, with JDK 9+ `java`/`javac` on PATH:

```sh
python3 tests/forge-1.13-1.15/run.py
python3 tests/forge-1.13-1.15/check_publication_sources.py --baseline <pre-fix-commit>
```

These are isolated helper tests and source guards, not in-game verification. Retest each reobfuscated release jar: enter port `31234` and max players `24`, confirm `ss` shows only `31234`, and confirm `/status` reports the same port and player limit. Also check a blank port, game mode and cheats selection, and world exit/reopen.
