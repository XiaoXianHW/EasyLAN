package org.xiaoxian.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.lan.ShareToLan;

public class GuiExitGame {
    @SubscribeEvent
    public void onGuiOpenEvent(ScreenEvent.Opening event) {
        Screen guiScreen = event.getScreen();
        if (guiScreen instanceof PauseScreen) {
            event.setNewScreen(new GuiInGameMenuModified());
        }
    }

    public static class GuiInGameMenuModified extends PauseScreen {

        public GuiInGameMenuModified() {
            super(true);
        }

        @Override
        protected void init() {
            super.init();

            Button originalButton = null;
            for (Renderable widget : this.renderables) {
                if (widget instanceof Button button) {
                    if (button.getMessage().getString().equals(I18n.get("menu.returnToMenu"))) {
                        originalButton = button;
                        break;
                    }
                }
            }

            if (originalButton != null) {
                int width = originalButton.getWidth();
                int height = originalButton.getHeight();
                int x = originalButton.getX();
                int y = originalButton.getY();

                this.renderables.remove(originalButton);
                this.removeWidget(originalButton);

                Button finalOriginalButton = originalButton;
                Button newButton = Button.builder(Component.translatable(I18n.get("menu.returnToMenu")), button -> {
                    ShareToLan.StopHttpAPIServer();
                    finalOriginalButton.onPress();
                }).bounds(x, y, width, height).build();

                this.addRenderableWidget(newButton);
            }
        }
    }
}
