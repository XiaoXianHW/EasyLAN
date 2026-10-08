#!/usr/bin/env python3
"""Source guards complement real helper tests; they do not prove live GUI behavior."""
from pathlib import Path
import subprocess
root=Path(__file__).resolve().parents[2]

def failures(version, read):
    prefix=f'versions/{version}/project/src/main/java/org/xiaoxian/'
    gui=read(prefix+'gui/GuiShareToLanEdit.java'); lan=read(prefix+'lan/ShareToLan.java')
    publish='shareToLAN' if version=='1.16.4' else 'publishServer'
    published='getPublic' if version=='1.16.4' else 'isPublished'
    port='getServerPort' if version=='1.16.4' else 'getPort'
    checks={
        'exactly one vanilla publish call':gui.count('server.'+publish+'(')==1,
        'no original random-port callback': 'finalOriginalButton.onPress()' not in gui,
        'no second listener': 'startLanPort(' not in lan and '.openLanEndpoint(' not in lan,
        'preserve selected game mode': 'LanPublication.selectedGameMode(this, ShareToLanScreen.class, GameType.class)' in gui,
        'preserve selected command flag': 'LanPublication.selectedCommands(this, ShareToLanScreen.class)' in gui,
        'duplicate publication blocked': 'publishing || server == null || server.'+published+'()' in gui,
        'failure returns before success chat/setup': 'if (port < 0)' in gui and gui.index('if (port < 0)') < gui.index('commands.publish.started') and 'return;' in gui[gui.index('if (port < 0)'):gui.index('commands.publish.started')],
        'setup requires published server': 'server == null || !server.'+published+'()' in lan,
        'report actual server port': 'return server.'+published+'() ? String.valueOf(server.'+port+'()) : null;' in lan,
        'HTTP snapshot uses actual port': 'String resolvedPort = getLanPort(server);' in lan,
    }
    return [key for key,passed in checks.items() if not passed]

for version in ['1.16.4','1.16.5','1.17.1','1.18.2']:
    errors=failures(version,lambda p:(root/p).read_text()); assert not errors,(version,errors)
    old_errors=failures(version,lambda p:subprocess.check_output(['git','-C',str(root),'show','360d048:'+p],text=True))
    assert 'no original random-port callback' in old_errors and 'no second listener' in old_errors,(version,old_errors)
    print(version+': publication source guards PASS; old-source negative control fails '+str(len(old_errors))+' guards as expected')
print('These tests are not full builds, production reobfuscation checks, or live game acceptance.')
