import org.xiaoxian.easylan.forge.version.LanPublication;
import java.io.IOException;

public final class LanPublicationRegression {
    enum Mode { SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR }
    static class OldScreen {
        private static final String LABEL = "ignored";
        private String obfuscatedMode = "creative";
        private boolean obfuscatedCheats = true;
    }
    static class ModernScreen {
        private static final Mode STATIC = Mode.SURVIVAL;
        private Mode obfuscatedMode = Mode.SPECTATOR;
        private boolean obfuscatedCheats;
    }
    static class ChildScreen extends ModernScreen { private boolean unrelated = true; }
    static class AmbiguousMode { Mode a; String b; }
    static class AmbiguousCommands { boolean a; boolean b; }
    static class Missing { int other; }
    interface Throwing { void run() throws Exception; }
    static void expectFailure(Throwing body) throws Exception {
        try { body.run(); } catch (ReflectiveOperationException | IllegalArgumentException | IOException expected) { return; }
        throw new AssertionError("Expected fail-closed behavior");
    }
    public static void main(String[] args) throws Exception {
        OldScreen old = new OldScreen(); ModernScreen modern = new ModernScreen();
        assert LanPublication.selectedGameMode(old, OldScreen.class, Mode.class) == Mode.CREATIVE;
        assert LanPublication.selectedCommands(old, OldScreen.class);
        assert LanPublication.selectedGameMode(modern, ModernScreen.class, Mode.class) == Mode.SPECTATOR;
        assert !LanPublication.selectedCommands(modern, ModernScreen.class);
        assert !LanPublication.selectedCommands(new ChildScreen(), ModernScreen.class);
        expectFailure(() -> LanPublication.selectedGameMode(new AmbiguousMode(), AmbiguousMode.class, Mode.class));
        expectFailure(() -> LanPublication.selectedCommands(new AmbiguousCommands(), AmbiguousCommands.class));
        expectFailure(() -> LanPublication.selectedGameMode(new Missing(), Missing.class, Mode.class));
        expectFailure(() -> LanPublication.selectedCommands(new Missing(), Missing.class));
        for (Mode mode : Mode.values()) {
            for (boolean commands : new boolean[] {false, true}) {
                assert LanPublication.publish("25599", mode, commands, () -> 30000,
                        (actualMode, actualCommands, selectedPort) -> actualMode == mode
                                && actualCommands == commands && selectedPort == 25599) == 25599;
            }
        }
        int[] calls = { 0, 0, 0 };
        int port = LanPublication.publish("25599", Mode.CREATIVE, true,
                () -> { calls[1]++; return 30000; }, (mode, cheats, selectedPort) -> {
                    calls[0]++; assert mode == Mode.CREATIVE; assert cheats; assert selectedPort == 25599; return true;
                });
        assert port == 25599 && calls[0] == 1 && calls[1] == 0;
        int blank = LanPublication.publish("", Mode.SURVIVAL, false,
                () -> { calls[1]++; return 30001; }, (mode, cheats, selectedPort) -> {
                    calls[0]++; assert mode == Mode.SURVIVAL; assert !cheats; assert selectedPort == 30001; return true;
                });
        assert blank == 30001 && calls[0] == 2 && calls[1] == 1;
        int failed = LanPublication.publish("25599", Mode.ADVENTURE, false, () -> 30000,
                (mode, cheats, selectedPort) -> { calls[0]++; return false; });
        if (failed > 0) calls[2]++; // The caller's success/setup branch must not run.
        assert failed == -1 && calls[0] == 3 && calls[2] == 0;
        for (String invalid : new String[] {"99", "65536", "abc", "-1"}) {
            expectFailure(() -> LanPublication.publish(invalid, Mode.SURVIVAL, false, () -> 30000,
                    (mode, cheats, selectedPort) -> { throw new AssertionError("Invalid input published"); }));
        }
        for (String valid : new String[] {"100", "65535"}) {
            assert LanPublication.publish(valid, Mode.SURVIVAL, false, () -> 30000,
                    (mode, cheats, selectedPort) -> selectedPort == Integer.parseInt(valid)) == Integer.parseInt(valid);
        }
        expectFailure(() -> LanPublication.publish("", Mode.SURVIVAL, false,
                () -> { throw new IOException("No port"); }, (mode, cheats, selectedPort) -> {
                    throw new AssertionError("Failed default-port selection published");
                }));
        System.out.println("LAN publication selection, single-publish, blank port, failure, boundaries, and fail-closed reflection: PASS");
    }
}
