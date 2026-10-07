# Targeted v1.6a report regression checks

Run `python -m unittest discover -s tests -v` from the group root.
These are source-contract guards, not Minecraft GUI tests or compilation.

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
