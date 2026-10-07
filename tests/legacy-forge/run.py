#!/usr/bin/env python3
"""Compile real bridge code with a minimal runtime stub; no Forge/network required."""
from pathlib import Path
import struct
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
shared = root / 'shared/forge-group/src/main/java/org/xiaoxian/easylan/forge/version'
with tempfile.TemporaryDirectory(prefix='easylan-legacy-regression-') as temp:
    temp = Path(temp)
    stub = temp / 'org/xiaoxian/EasyLAN.java'
    stub.parent.mkdir(parents=True)
    stub.write_text('''package org.xiaoxian;
public class EasyLAN {
    private static final RuntimeState STATE = new RuntimeState();
    public static RuntimeState getRuntimeState() { return STATE; }
    public static class RuntimeState {
        private String port = "";
        public String getLanPort() { return port; }
        public void setLanPort(String value) { port = value; }
    }
}
''')
    for version, release in [('1.16.4', 8), ('1.16.5', 8), ('1.17.1', 16), ('1.18.2', 17)]:
        output = temp / version
        output.mkdir()
        subprocess.run(['javac', '--release', str(release), '-d', str(output), str(stub),
                        str(shared / 'VersionBridge.java'), str(shared / 'ReflectionVersionBridgeSupport.java'),
                        str(root / f'versions/{version}/project/src/main/java/org/xiaoxian/easylan/forge/version/VersionBridgeImpl.java'),
                        str(Path(__file__).with_name('ReflectionBridgeRegression.java'))], check=True)
        bridge_class = output / 'org/xiaoxian/easylan/forge/version/VersionBridgeImpl.class'
        major = struct.unpack('>H', bridge_class.read_bytes()[6:8])[0]
        assert major == release + 44, (version, major)
        subprocess.run(['java', '-cp', str(output), 'ReflectionBridgeRegression', version], check=True)
    build = (root / 'versions/1.17.1/project/build.gradle').read_text()
    assert 'options.release = 16' in build, '1.17.1 must emit Java 16-compatible bytecode'
    print('1.17.1 Java 16 build setting and isolated bridge class version: PASS')
    print('These are isolated Java regression tests, not packaged mod or in-game verification.')
