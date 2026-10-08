# Legacy Forge reported-defect regression

Scope: failed 1.16.4, 1.16.5, 1.17.1, and 1.18.2 rows in the supplied August 2026 manual report.

## Existing fixes versus the reported v1.6a

- The supplied 1.17.1 crashes explicitly say class version 61 (Java 17) cannot run on Java 16 (maximum 60). Commit `69f7a12` already adds `options.release = 16`; no duplicate production edit is needed.
- Commit `8cf003a` already adds production player-list getter aliases and the field fallback, addressing the reported player count stuck at 8. A negative control using the source immediately before that commit fails the production max-player test; current source passes.
- The supplied 1.16.x/1.18.2 logs also contain custom-port errors. The pre-fix baseline only searches development endpoint names. The new fix includes the production names `func_151265_a` (1.16.x) and `m_9711_` (1.17/1.18). The endpoint regression fails before this edit and passes afterward.
- The supplied inputs contain logs/crashes, not the original tested jars. This comparison is against logged artifact identity and repository history, not a byte-for-byte comparison with those jars.

Mapping evidence: official Forge MCP config archives, `config/joined.tsrg`:
- https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/1.16.5/mcp_config-1.16.5.zip
- https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/1.18.2/mcp_config-1.18.2.zip

## Run focused tests

Put JDK 17 or newer's `java` and `javac` on PATH, then run from the repository root:

```sh
python3 tests/legacy-forge/run.py
```

The test compiles the actual shared bridge and each version-specific implementation against a minimal EasyLAN runtime-state stub. It covers development and production player-list names, inherited methods and fields, field-only fallback, custom endpoint arguments, successful port recording, unsupported endpoints, and failed binds. It also checks the 1.17.1 build release setting and the compiled isolated bridge's class version.

This is not a Forge build, packaged-jar bytecode audit, or in-game test. Compilation with `--release 16` alone cannot prove all classes in a distributable jar are Java 16 compatible.

## Remaining in-game verification

Build each version using its checked-in wrapper and required JDK (1.16.x Java 8, 1.17.1/1.18.2 Java 17 build toolchains; 1.17.1 release target 16). Test a reobfuscated release jar, because development mappings would hide the original reflection defect.

For each version, follow the supplied manual procedure: launch without crash; confirm the EasyLAN entry and config screen, toggle online mode off and save/reopen config; open a world and enter a non-default port (e.g. 25570) and player limit (e.g. 24); confirm LAN chat output and `http://localhost:28960/status` match the actual port, player limit, and online mode, and check `/playerlist`; quit the world and confirm both HTTP endpoints refuse connections. For 1.17.1 also launch on Java 16 and audit every class in the packaged jar for major version <= 60.

Do not mark any manual-report row passed based on the isolated tests alone.

## Validation recorded 2026-10-07 (UTC)

- Focused regression: PASS for all four version implementations, including Java release targets 8/8/16/17. Negative controls fail on the pre-existing max-player fix's parent and on the new endpoint fix's parent, respectively.
- Full build and reobfuscation: PASS for all four versions: 1.16.4 (checked wrapper 4.10.3/JDK 8), 1.16.5 (Gradle 8.7 launcher/JDK 8 compiler), 1.17.1 (Gradle 7.5.1/JDK 17 with release 16), and 1.18.2 (checked wrapper 7.5.1/JDK 17).
- Packaged class audit: 1.16.4 has 72 classes, maximum major 52; 1.16.5 has 71 classes, maximum major 52; 1.17.1 has 70 classes, maximum major 60; 1.18.2 has 70 classes, maximum major 61. All four packaged bridges contain both production endpoint aliases.
- Current branch pins Forge 35.1.37 / 36.2.42 / 37.1.1 / 40.2.9 for these four versions, whereas supplied reports use 35.1.4 / 36.2.34 / 37.1.1 / 40.3.0. These successful builds do not establish runtime compatibility with every reported loader patch version.
- 1.17.1 recovered verification: earlier transient execution-session cancellations were resolved by resuming the build in a stable terminal. The full build and packaged-class audit passed. Every class is Java 16-compatible (major <= 60), directly addressing the original class 61 loading failure. No Java 16 live client launch was performed.
- A separate production Forge 1.16.5-36.2.34 client loaded the patched reobfuscated jar, entered a world, and completed the UI smoke flow. Real game chat/logs show custom port 25599, maximum players 99, and online mode false; world exit saved cleanly. Independent HTTP/socket probes were not performed because the browser route was blocked. This is a live launch/UI/LAN-output smoke pass, not a complete pass of the supplied manual procedure.


## Actual published-port correction (2026-10-08 UTC)

The archived Forge 1.17.1 log shows vanilla `Started serving on 43221` while EasyLAN reports the requested `25599`. The previous flow opened a second endpoint before invoking the original vanilla button, which then published and advertised a random port. Earlier chat-only smoke evidence must not be treated as a full LAN-port pass.

All four legacy versions now publish exactly once through the strongly typed vanilla `shareToLAN` / `publishServer` API using the requested port. Empty input selects an available default port. The vanilla screen's selected game mode and cheat permission are read from its exact private field types, preserving both settings independently of development/production field names. Missing or ambiguous fields fail before publishing. No Mixin or access transformer was added.

Settings, API startup and success messages run only after successful publication. The original random-port callback and explicit second-listener path are no longer invoked. Repeated clicks are blocked while publishing and after the server is published. Chat and the HTTP snapshot read the actual published server port rather than trusting input text.

Focused tests compile the real production `LanPublication` helper at Java releases 8/8/16/17. They cover all four game modes, cheats on/off, one publication only, blank default selection, failed publication without a success branch, invalid and boundary ports, and missing/ambiguous private-field layouts. Source guards for all four versions pass and fail against pre-fix commit `360d048` (including the original callback and second-endpoint checks). These checks do not establish GUI behavior, final production mapping, actual multicast discovery, or complete runtime acceptance.

For every newly built production JAR, repeat the manual procedure and require the requested port, vanilla `Started serving on` / publication chat, EasyLAN chat, HTTP snapshot, and LAN discovery advertisement to agree. Verify selected game mode and cheats on/off, blank-port automatic selection, a bind failure with no success output or API startup, repeated clicks, and world exit/reopen. HTTP/socket acceptance remains separately required when available.
