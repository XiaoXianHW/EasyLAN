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
  creation and LAN startup showed the requested port 25599 and maximum players 99.
  Later production-runtime inspection found that this was insufficient evidence:
  vanilla published a random port and its server-level maximum remained eight.
- Actual rendered chat and Render-thread logs show the essential local LAN block
  before the public-IP service returned Unknown. Default online mode remained true.
  Save/Quit saved all dimensions and the client exited normally.
- This does not certify a full source-recompilation build, packaged Neo runtime,
  forced live network timeouts, HTTP endpoint contents, second-client connections,
  online-mode toggling, or the complete manual matrix.

## NeoForge 1.21.11 effective LAN settings repair

`test_neoforge_12111_settings.py` executes the production publish callback,
version bridge and mixin limit callback with deterministic Java doubles. It
checks custom and blank ports, bind failure, stale cached ports, unchanged player
list identity, validation and per-world limit reset. Negative controls reintroduce
the random publish-port bug and suppress the server getter override; both must
fail. These tests do not execute Mixin transformation or simulate a game client.

Exact NeoForge 21.11.45 production bytecode was inspected: IntegratedServer's
getMaxPlayers returns a literal eight, PlayerList delegates to that getter,
player admission uses PlayerList.getMaxPlayers, and vanilla server status uses
MinecraftServer.getMaxPlayers. A version-local required client mixin now supplies
the per-world configured limit to that shared server getter. The live PlayerList
is retained. The screen publishes the selected port directly and no longer opens
a second listener. Port reporting and API snapshots read the published server
port, rather than trusting the text field or stale runtime cache.

Required production retest: custom25599/max99, blank port/default max in another
world, actual vanilla hosted port, live listen socket, server/player-list maximum
getters, saved-world exit/reopen, and absence of mixin errors or obsolete max-field
warnings. Runtime logs include the actual three getter values for this purpose.
HTTP endpoint and second-client verification remain separate and must not bypass
an access restriction or accept an unapproved multiplayer warning.

## Remaining NeoForge publish-port paths

The same dual-listener defect affected all four older targets. Version 1.21.5
now publishes its selected custom port directly. The 1.20.1 override and shared
1.20.6/1.21.1 screen preserve the hidden vanilla port EditBox and send custom
input through its existing responder before invoking vanilla's Start action.
They only continue EasyLAN setup when the server was actually published. The
shared setup no longer opens a second listener, and port resolution ignores
cached inputs and previous-world log entries.

`test_neoforge_older_settings.py` executes both inherited-screen handlers and the
1.21.5 direct handler, including failed publication and missing vanilla input.
Negative controls omit the vanilla input update and must fail. Shared bridge
checks cover Mojang and 1.20.1 SRG names plus stale-port clearing.

Exact production bytecode for 47.1.106, 20.6.139, 21.1.248 and 21.5.98 confirms
that the older server maximum delegates to PlayerList's writable maximum field.
Those versions keep their existing limit mechanism and receive no new mixin.
All versions now log the actual published port and server/player-list maximum.
Older rebuilt artifacts and production GUI retests remain required; the focused
Java doubles do not establish a full Minecraft runtime pass.
