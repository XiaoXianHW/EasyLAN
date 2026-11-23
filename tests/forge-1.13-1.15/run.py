#!/usr/bin/env python3
"""Compile real bridge code with a minimal runtime stub; no Forge/network required."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
here = Path(__file__).resolve().parent
shared = root / 'shared/forge-group/src/main/java/org/xiaoxian/easylan/forge/version'
with tempfile.TemporaryDirectory(prefix='easylan-forge-113-regression-') as temp:
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
    for version in ['1.13.2', '1.14.2', '1.14.4', '1.15.2']:
        output = temp / version
        output.mkdir()
        subprocess.run(['javac', '--release', '8', '-d', str(output), str(stub),
                        str(shared / 'VersionBridge.java'),
                        str(shared / 'ReflectionVersionBridgeSupport.java'),
                        str(shared / 'LanPublication.java'),
                        str(root / f'versions/{version}/project/src/main/java/org/xiaoxian/easylan/forge/version/VersionBridgeImpl.java'),
                        str(root / 'shared/core/src/main/java/org/xiaoxian/easylan/core/ValidationRules.java'),
                        str(here / 'ReflectionBridgeRegression.java'),
                        str(here / 'LanPublicationRegression.java')], check=True)
        subprocess.run(['java', '-cp', str(output), 'ReflectionBridgeRegression', version], check=True)
        subprocess.run(['java', '-ea', '-cp', str(output), 'LanPublicationRegression'], check=True)
    print('These are isolated Java regression tests, not packaged mod or in-game verification.')

subprocess.run(['python3', str(here / 'check_publication_sources.py')], check=True)
