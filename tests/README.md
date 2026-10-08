# Local regression checks

Run `JAVA_HOME=/path/to/jdk-21 python3 -m unittest discover -s tests -v`.

- Source guards cover existing world lifecycle, MOTD, and input event ownership.
- `test_lan_publication.py` extracts the actual production publication and live-port getters, executes them with deterministic Java doubles, and reintroduces the random-port defect as a negative control. It covers selected/default/leading-zero ports and the 100/1023/1024 range boundaries, failed publication, missing port controls where applicable, repeated clicks, all four game modes with commands on/off on 1.16, denied/failed low-port binds, invalid inputs, stale cached ports, and live player-list reporting.
- On 1.21.11, `test_player_limit_12111.py` compiles the actual bridge/mixin/interface against minimal Java doubles. It checks the admission getter, invalid values, per-server state, required mixin registration, and negative controls that remove application of the setting.

These are offline regression checks, not full game acceptance. Build each requested target, then verify its newly built JAR in the actual Fabric client. Confirm vanilla's published-port log, live listener, LAN discovery port, and live player capacity agree. HTTP remains unverified unless that test is authorized and available.
