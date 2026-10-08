/** Event-lifecycle test double. The runner compiles the unchanged production hook and constructor. */
public class LanNavigationRegression {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static void main(String[] args) {
        String version = args[0];
        boolean repaired = version.equals("1.16.4");
        Screen pause = version.startsWith("1.16.") ? new IngameMenuScreen(true) : new PauseScreen(true);
        CLIENT.setScreen(pause);
        for (int attempt = 0; attempt < 3; attempt++) {
            CLIENT.setScreen(new ShareToLanScreen(pause));
            check(CLIENT.screen instanceof GuiShareToLanEdit.GuiShareToLanModified,
                    "LAN form was not replaced");
            ShareToLanScreen form = (ShareToLanScreen) CLIENT.screen;
            if (repaired) check(form.parent == pause, "Cancel must preserve the exact previous screen");
            form.cancel();
            check(CLIENT.screen instanceof IngameMenuScreen || CLIENT.screen instanceof PauseScreen,
                    "Cancel must leave the LAN form for the game menu");
            pause = CLIENT.screen;
        }
        pause.backToGame();
        check(CLIENT.screen == null, "Back to Game must return to the world");

        if (repaired) {
            // A direct LAN-screen open from the world must also preserve a null parent.
            CLIENT.setScreen(new ShareToLanScreen(null));
            ((ShareToLanScreen) CLIENT.screen).cancel();
            check(CLIENT.screen == null, "Cancel must preserve a null previous screen");
        }

        // Escape's vanilla close route is unchanged; neither null transition may reopen the form.
        for (int attempt = 0; attempt < 3; attempt++) {
            CLIENT.setScreen(pause);
            CLIENT.setScreen(new ShareToLanScreen(pause));
            CLIENT.screen.escape();
            check(CLIENT.screen == null, "Escape must return to the world without replacing null");
        }

        if (repaired) {
            // A previously modified screen may be reopened or seen by a repeated event subscriber.
            GuiShareToLanEdit.GuiShareToLanModified modified =
                    new GuiShareToLanEdit.GuiShareToLanModified(pause);
            CLIENT.setScreen(modified);
            check(CLIENT.screen == modified, "Do not rewrap an already modified LAN form");
            modified.cancel();
            check(CLIENT.screen == pause, "Reopened modified form must keep its original parent");
            Object event = Events.create(new ShareToLanScreen(pause));
            Events.handle(event);
            Screen firstReplacement = Events.get(event);
            Events.handle(event);
            check(Events.get(event) == firstReplacement, "Repeated event delivery must be idempotent");
        }

        Screen unrelated = new Screen();
        CLIENT.setScreen(unrelated);
        check(CLIENT.screen == unrelated, "Unrelated screens must be left alone");
        CLIENT.setScreen(null);
        check(CLIENT.screen == null, "Null/closed screen must be left alone");
        System.out.println(version + " navigation event regression: PASS");
    }

    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }
}

class Screen {
    void escape() { Minecraft.getInstance().setScreen(null); }
    void backToGame() { Minecraft.getInstance().setScreen(null); }
}
class PauseScreen extends Screen { PauseScreen(boolean ignored) {} }
class IngameMenuScreen extends Screen { IngameMenuScreen(boolean ignored) {} }
class ShareToLanScreen extends Screen {
    final Screen parent;
    ShareToLanScreen(Screen parent) { this.parent = parent; }
    void cancel() { Minecraft.getInstance().setScreen(parent); }
}
class Minecraft {
    private static final Minecraft INSTANCE = new Minecraft();
    Screen screen;
    Screen currentScreen;
    static Minecraft getInstance() { return INSTANCE; }
    void setScreen(Screen incoming) {
        // All four official Forge patches post the event before assigning the new screen.
        Object event = Events.create(incoming);
        Events.handle(event);
        screen = currentScreen = Events.get(event);
    }
}
class GuiOpenEvent {
    private Screen gui;
    GuiOpenEvent(Screen gui) { this.gui = gui; }
    Screen getGui() { return gui; }
    void setGui(Screen gui) { this.gui = gui; }
}
class ScreenOpenEvent {
    private Screen screen;
    ScreenOpenEvent(Screen screen) { this.screen = screen; }
    Screen getScreen() { return screen; }
    void setScreen(Screen screen) { this.screen = screen; }
}
