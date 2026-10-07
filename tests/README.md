# Targeted v1.6a report regressions

Run `python -m unittest discover -s tests -v` from the group root with a JDK 17+
`java` on PATH (or JAVA_HOME). Source-contract guards cover every local override.
`test_lan_chat_order.py` extracts the production sendLanInfo method unchanged and
executes it with deterministic Java doubles. It tests both shared and 1.21.11
implementations, with HTTP API on/off and a failing external address service.
This is a focused executable logic test, not a Minecraft integration test.

The current branch already limits settings entries to world selection, restores
standard input dispatch and enqueues chat on the client thread. Remaining fixes:
- Opaque settings background prevents prior-frame bleed (shared, including 1.20.1)
- 1.21.11 override now sends local LAN details before external address lookups,
  matching the already-correct shared implementation

## Required runtime scope

- NeoForge 1.20.1 (47.1.106): world selection → settings has no old menu text/list;
  toggles and MOTD save/reload, Back/Esc/reopen and resize.
- 1.20.6 (20.6.139), 1.21.1 (21.1.248), 1.21.5 (21.5.98), 1.21.11 (21.11.45):
  create-world tabs clean, settings entry on world selection, input focus/editing.
- 1.21.11: start LAN with a distinct available custom port, max players 37,
  online mode false. Chat must immediately show success and local settings even
  when internet/public-IP services are unavailable. Test LanOutput true and false;
  false should suppress the full info block. Repeat with HttpAPI true and false.
- With HttpAPI on, localhost:28960/status and /playerlist must match the world;
  after leaving, both must refuse connections. Repeat another world to catch
  stale chat/API state. Test entry with no saves, plus normal existing saves.

Do not report the entire matrix passed from these source/logic tests alone.


## Validation recorded 2026-10-07 (UTC)

- Focused source/Java logic regressions passed, including external-address failure
  and chat-order checks. NeoForge 1.21.11 binary-artifact compilation passed.
- Report-matched NeoForge 1.21.11/21.11.45 development client ran using ModDev's
  supported binary-artifact mode. Create-world tabs were unobstructed; world
  creation and LAN startup succeeded with custom port 25599 and maximum players 99.
- Actual rendered chat and Render-thread logs show the essential local LAN block
  before the public-IP service returned Unknown. Default online mode remained true.
  Save/Quit saved all dimensions and the client exited normally.
- This does not certify a full source-recompilation build, packaged Neo runtime,
  forced live network timeouts, HTTP endpoint contents, second-client connections,
  online-mode toggling, or the complete manual matrix.
