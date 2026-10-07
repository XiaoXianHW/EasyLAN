# Targeted v1.6a report regression checks

Run `python -m unittest discover -s tests -v` from the group root.
These are source-contract guards, not Minecraft GUI tests or compilation.

`test_lan_chat_runtime.py` also executes unchanged production chat methods with
Java dependency doubles using JDK 17+ source-file launch. It verifies all six
reported-failing targets: 1.19.4, 1.20.1, 1.20.6, 1.21.1, 1.21.5 and 1.21.11.
It checks that executor-thread messages are queued on the Minecraft client,
formatting and order are preserved, queued output tolerates GUI teardown, and
local LAN details use the bridge-resolved player count before external lookups.
Both HTTP enabled and disabled modes are covered with a failing public lookup.
These deterministic checks are not packaged-client runtime verification.

Current branch already fixes the create-world button intrusion (all modern Forge
versions), standard screen input dispatch, and Forge 1.21.11 per-event buses.
The new change makes the Forge 1.20.1 settings background opaque: alpha C0 still
composited the previous frame into the settings view.

## Required runtime scope

- Forge 1.20.1 (47.4.10): open world selection → settings; verify no old list/text
  visible; toggle, edit MOTD, save, return/reopen. Repeat after resize and Back/Esc.
- Forge 1.19.4, 1.20.6, 1.21.1, 1.21.5: first-world creation and normal creation;
  no EasyLAN button over tabs; world-selection entry remains accessible.
- Forge 1.21.5: each input once (no doubling), backspace/delete, arrows, Ctrl+A,
  paste, switch focus, resize, reopen; settings MOTD and both LAN fields.
- Forge 1.21.11 (61.2.0): client loads without event-bus exception; open settings,
  create world, open LAN, leave world; confirm start/stop handlers run.
- For versions touched above, validate chosen port, max players and online-mode
  output; `/status` and `/playerlist` on localhost:28960 while LAN active, and
  connection refusal after exit. Do not count a title-screen launch as full pass.

## Validation recorded 2026-10-07 (UTC)

- Focused source guards passed. Forge 1.20.1 and 1.21.11 builds passed.
- Report-matched Forge 1.20.1/47.4.10 development client: created and entered a
  world, then verified the opaque settings background, Load Config, Back and
  reopening. No old world-list text bled through. Client shut down normally.
- Separate Forge 1.21.1/52.1.0 development-client smoke on the unchanged baseline
  source verified clean create-world tabs, settings save/reopen, single input
  dispatch, invalid-port rejection, and LAN chat with custom port 25599, maximum
  players 99 and online mode false. World exit saved cleanly.
- These are focused development-client checks. Packaged production runtime,
  independent socket/API probes, and the complete manual matrix remain unverified.

## Recovered checkpoint follow-up, 2026-10-07 (UTC)

The restored checkpoint at `17c2c9e` already put local LAN output ahead of public
address lookups. However, all six reported-failing modern Forge variants still
updated the chat GUI directly from an executor, and their chat maximum-player
line used `server.getMaxPlayers()` instead of the bridge-resolved value already
used by HTTP status. The follow-up queues chat using the same client scheduling
pattern as NeoForge and makes the two outputs agree. The previously passing
1.19.2 target is outside this follow-up and is unchanged.

- Negative control: the new executable checks fail in all twelve version/case
  combinations against checkpoint production code.
- Patched source: all six unittest cases pass, including the twelve executable
  version/case combinations. `git diff --check` passes.
- All six changed versions require new production JARs and runtime acceptance.
  In particular, the older 1.21.5 production smoke evidence does not validate
  this follow-up. Forge 1.21.11 must be built and exercised with Forge 61.2.0.
- The first 1.21.5 full compile caught a missing resolver helper in the five
  pre-1.21.11 variants. Those variants now contain the production resolver.
  Their existing HTTP server getter is unchanged. The harness now extracts the
  actual helper and checks fallback for unavailable bridge
  values; it no longer supplies a resolver double that could hide this error.
