#!/usr/bin/env python3
"""Compile actual production event hooks against a documented Forge lifecycle double.

No Gradle, network, Minecraft startup, or copy of the production hook is needed.
Negative controls compile the old hook and deliberate parent/rewrapping regressions.
"""
from pathlib import Path
import argparse
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
VERSIONS = [('1.16.4', 8), ('1.16.5', 8), ('1.17.1', 16), ('1.18.2', 17)]


def extract_method(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


def run(version, release, source, mutation=None):
    hook = extract_method(source, 'public void onGuiOpenEvent(')
    constructor = extract_method(source, 'public GuiShareToLanModified(')
    if mutation == 'incoming-parent':
        field = 'currentScreen' if version == '1.16.4' else 'screen'
        hook = hook.replace('Minecraft.getInstance().' + field, 'incomingScreen')
    elif mutation == 'rewrap':
        hook = hook.replace('\n                && !(incomingScreen instanceof GuiShareToLanModified)', '')
    event = 'ScreenOpenEvent' if version == '1.18.2' else 'GuiOpenEvent'
    getter = 'getScreen' if version == '1.18.2' else 'getGui'
    with tempfile.TemporaryDirectory(prefix='easylan-navigation-') as temp:
        temp = Path(temp)
        production = temp / 'GuiShareToLanEdit.java'
        production.write_text('public class GuiShareToLanEdit {\n' + hook + '\n'
                              'public static class GuiShareToLanModified extends ShareToLanScreen {\n'
                              + constructor + '\n}\n}\n')
        adapter = temp / 'Events.java'
        adapter.write_text(f'''class Events {{
    static Object create(Screen screen) {{ return new {event}(screen); }}
    static void handle(Object event) {{ new GuiShareToLanEdit().onGuiOpenEvent(({event}) event); }}
    static Screen get(Object event) {{ return (({event}) event).{getter}(); }}
}}
''')
        subprocess.run(['javac', '-J-XX:ActiveProcessorCount=2', '--release', str(release),
                        '-d', str(temp), str(production), str(adapter),
                        str(Path(__file__).with_name('LanNavigationRegression.java'))], check=True)
        return subprocess.run(['java', '-XX:ActiveProcessorCount=2', '-cp', str(temp),
                               'LanNavigationRegression', version], capture_output=True, text=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--baseline', help='Also require hooks from this git revision to fail')
    args = parser.parse_args()
    for version, release in VERSIONS:
        relative = f'versions/{version}/project/src/main/java/org/xiaoxian/gui/GuiShareToLanEdit.java'
        source = (ROOT / relative).read_text()
        result = run(version, release, source)
        assert result.returncode == 0, result.stdout + result.stderr
        print(result.stdout.strip(), flush=True)
        mutations = [('incoming-parent', 'Cancel must preserve the exact previous screen'),
                     ('rewrap', 'Do not rewrap an already modified LAN form')] if version == '1.16.4' else []
        for mutation, expected in mutations:
            result = run(version, release, source, mutation)
            assert result.returncode != 0 and expected in result.stderr, (version, mutation, result.stderr)
            print(f'{version} {mutation} negative control: expected FAIL', flush=True)
        if args.baseline:
            old = subprocess.check_output(['git', 'show', f'{args.baseline}:{relative}'], cwd=ROOT, text=True)
            result = run(version, release, old)
            if version == '1.16.4':
                assert result.returncode != 0 and 'Cancel must preserve the exact previous screen' in result.stderr, result.stderr
                print(f'{version} baseline {args.baseline} negative control: expected FAIL', flush=True)
            else:
                assert result.returncode == 0, result.stderr
                print(f'{version} baseline {args.baseline} unchanged navigation: PASS', flush=True)
    print('Event/constructor regression only; actual buttons, input handling and game launch need live retests.')


if __name__ == '__main__':
    main()
