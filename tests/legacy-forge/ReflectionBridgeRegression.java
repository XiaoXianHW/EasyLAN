import java.io.IOException;
import java.net.InetAddress;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.easylan.forge.version.VersionBridgeImpl;

/** Dependency-free checks against the real bridge and each version's implementation. */
public class ReflectionBridgeRegression {
    static class DevList { private int maxPlayers = 8; }
    static class OldList { private int field_72405_c = 8; }
    static class NewList { private int f_11193_ = 8; }
    static class DevServer { DevList list = new DevList(); public Object getPlayerList() { return list; } }
    static class OldServer { OldList list = new OldList(); public Object func_184103_al() { return list; } }
    static class NewServer { NewList list = new NewList(); public Object m_6846_() { return list; } }
    static class OldInherited extends OldServer { }
    static class NewInherited extends NewServer { }
    static class FieldOnly { private Object players; FieldOnly(Object players) { this.players = players; } }
    static class Endpoint {
        int port;
        void record(InetAddress address, int port) {
            check(address.isAnyLocalAddress(), "bind to wildcard address");
            this.port = port;
        }
    }
    static class DevEndpoint extends Endpoint { public void startTcpServerListener(InetAddress a, int p) { record(a, p); } }
    static class McpEndpoint extends Endpoint { public void addEndpoint(InetAddress a, int p) { record(a, p); } }
    static class OldEndpoint extends Endpoint { public void func_151265_a(InetAddress a, int p) { record(a, p); } }
    static class NewEndpoint extends Endpoint { public void m_9711_(InetAddress a, int p) { record(a, p); } }
    static class InheritedEndpoint extends NewEndpoint { }
    static class FailingEndpoint extends Endpoint { public void startTcpServerListener(InetAddress a, int p) throws IOException { throw new IOException("address in use"); } }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        boolean old = args[0].startsWith("1.16.");
        VersionBridgeImpl bridge = new VersionBridgeImpl();
        DevServer dev = new DevServer();
        check(bridge.setMaxPlayers(dev, 24) && dev.list.maxPlayers == 24, "development max players");
        if (old) {
            OldInherited prod = new OldInherited();
            check(bridge.setMaxPlayers(prod, 32) && prod.list.field_72405_c == 32, "production inherited old max players");
            OldList fallback = new OldList();
            check(bridge.setMaxPlayers(new FieldOnly(fallback), 48) && fallback.field_72405_c == 48, "old field fallback");
        } else {
            NewInherited prod = new NewInherited();
            check(bridge.setMaxPlayers(prod, 32) && prod.list.f_11193_ == 32, "production inherited new max players");
            NewList fallback = new NewList();
            check(bridge.setMaxPlayers(new FieldOnly(fallback), 48) && fallback.f_11193_ == 48, "new field fallback");
        }
        check(!bridge.setMaxPlayers(new Object(), 32), "unsupported player list must fail");
        System.out.println(args[0] + ": max-player regression PASS");
        Endpoint[] endpoints = {new DevEndpoint(), new McpEndpoint(), old ? new OldEndpoint() : new NewEndpoint(), old ? new OldEndpoint() : new InheritedEndpoint()};
        for (Endpoint endpoint : endpoints) {
            EasyLAN.getRuntimeState().setLanPort("");
            bridge.openLanEndpoint(endpoint, 25570);
            check(endpoint.port == 25570, "custom endpoint port");
            check("25570".equals(EasyLAN.getRuntimeState().getLanPort()), "runtime port after success");
        }
        for (Object endpoint : new Object[] {new Object(), new FailingEndpoint()}) {
            EasyLAN.getRuntimeState().setLanPort("previous");
            try { bridge.openLanEndpoint(endpoint, 25571); throw new AssertionError("endpoint failure was swallowed"); }
            catch (IOException expected) { }
            check("previous".equals(EasyLAN.getRuntimeState().getLanPort()), "failed bind cannot update runtime port");
        }
        System.out.println(args[0] + ": development/production endpoint regression PASS");
    }
}
