#!/usr/bin/env python3
"""Source guards complement real helper tests; they do not prove live GUI behavior."""
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[2]
baseline = sys.argv[sys.argv.index('--baseline') + 1] if '--baseline' in sys.argv else None


def failures(version, read):
    prefix = f'versions/{version}/project/src/main/java/org/xiaoxian/'
    gui = read(prefix + 'gui/GuiShareToLanEdit.java')
    lan = read(prefix + 'lan/ShareToLan.java')
    bridge = read(prefix + 'easylan/forge/version/VersionBridgeImpl.java')
    screen = 'GuiShareToLan' if version == '1.13.2' else 'ShareToLanScreen'
    char_typed = gui[gui.index('public boolean charTyped'):gui.index('public boolean mouseClicked')]
    checks = {
        'exactly one vanilla publish call': gui.count('server.shareToLAN(') == 1,
        'no original random-port callback': 'finalOriginalButton.on' not in gui,
        'no second listener': 'startLanPort(' not in lan and '.openLanEndpoint(' not in lan,
        'preserve selected game mode': f'LanPublication.selectedGameMode(this, {screen}.class, GameType.class)' in gui,
        'preserve selected command flag': f'LanPublication.selectedCommands(this, {screen}.class)' in gui,
        'duplicate publication blocked': 'publishing || server == null || server.getPublic()' in gui,
        'failure returns before success chat/setup': 'if (port < 0)' in gui
            and gui.index('if (port < 0)') < gui.index('commands.publish.started'),
        'setup requires published server': 'server == null || !server.getPublic()' in lan,
        'report actual server port': 'return server.getPublic() ? String.valueOf(server.getServerPort()) : null;' in lan,
        'HTTP snapshot uses actual port': 'String resolvedPort = getLanPort(server);' in lan,
        'typing keeps partial numbers': 'setText(' not in char_typed,
        'production player-list getter': '"func_184103_al"' in bridge,
    }
    return [key for key, passed in checks.items() if not passed]


for version in ['1.13.2', '1.14.2', '1.14.4', '1.15.2']:
    errors = failures(version, lambda p: (root / p).read_text())
    assert not errors, (version, errors)
    message = version + ': publication source guards PASS'
    if baseline:
        old = failures(version, lambda p: subprocess.check_output(['git', '-C', str(root), 'show', baseline + ':' + p], text=True))
        assert 'no second listener' in old and 'production player-list getter' in old, (version, old)
        message += f'; baseline {baseline} fails {len(old)} guards as expected'
    print(message)
print('These tests are not full builds, production reobfuscation checks, or live game acceptance.')
