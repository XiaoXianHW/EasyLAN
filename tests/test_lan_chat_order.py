"""Execute the production sendLanInfo method with deterministic dependency doubles.

No game client is used. This verifies local chat ordering even when public-address
services throw, while the separate Minecraft smoke test verifies rendering.
"""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]

HARNESS = r'''
import java.util.*;
public class LanChatHarness {
    static boolean HttpAPI = true, onlineMode = false;
    static List<String> messages = new ArrayList<>();
    static class IntegratedServer {}
    static class GuiShareToLanEdit { static String PortText = "25569"; }
    static class I18n { static String get(String key) { return key; } }
    static class ChatUtil { static void sendMsg(String value) { messages.add(value); } }
    static class PublicLookupFailure extends RuntimeException {}
    static class NetworkUtil {
        static String getLocalIpv4() { return "192.168.1.2"; }
        static String getLocalIpv6() { return "::1"; }
        static String getPublicIPv4() {
            // Simulate a failure before public services can return anything.
            throw new PublicLookupFailure();
        }
        static boolean checkIpIsPublic() { throw new AssertionError("lookup must stop on failure"); }
    }
    static class Executor { void submit(Runnable task) { task.run(); } }
    static class RuntimeState {
        Executor openExecutorService(int count) { return new Executor(); }
        String getHttpApiPort() { return "28960"; }
    }
    static class EasyLAN { static RuntimeState getRuntimeState() { return new RuntimeState(); } }
    static String getLanPort(IntegratedServer server) { return "25569"; }
    static int resolveMaxPlayers(IntegratedServer server) { return 37; }
    static boolean isBlank(String value) { return value == null || value.isEmpty(); }
    static String safeValue(Object value) { return value == null ? "" : value.toString(); }
    PRODUCTION_METHOD
    public static void main(String[] args) {
        for (boolean http : new boolean[] {true, false}) {
            HttpAPI = http;
            messages.clear();
            try { new LanChatHarness().sendLanInfo(new IntegratedServer()); }
            catch (PublicLookupFailure expected) { }
            String joined = String.join("\n", messages);
            for (String expected : List.of("Successfully", "25569", "37", "false")) {
                if (!joined.contains(expected)) throw new AssertionError("Missing local status: " + expected);
            }
            if (http && (!joined.contains("/status") || !joined.contains("/playerlist")))
                throw new AssertionError("API endpoints were delayed behind public lookup");
            if (!http && joined.contains("Http-Api")) throw new AssertionError("Disabled API advertised");
        }
        System.out.println("PASS: local status survives public lookup failure (HTTP on/off)");
    }
}
'''

class LanChatOrder(unittest.TestCase):
    def test_shared_and_12111_production_methods(self):
        java = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if os.environ.get('JAVA_HOME') else 'java'
        for path in ROOT.glob('**/ShareToLan.java'):
            with self.subTest(path=str(path.relative_to(ROOT))):
                source = path.read_text()
                method = 'private void sendLanInfo' + source.split('private void sendLanInfo', 1)[1].split('private void startHttpApi', 1)[0]
                with tempfile.TemporaryDirectory() as tmp:
                    harness = Path(tmp) / 'LanChatHarness.java'
                    harness.write_text(HARNESS.replace('PRODUCTION_METHOD', method))
                    result = subprocess.run([java, str(harness)], capture_output=True, text=True, timeout=30)
                    self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

if __name__ == '__main__':
    unittest.main()
