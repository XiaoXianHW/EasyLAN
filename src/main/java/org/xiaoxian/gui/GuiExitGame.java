package org.xiaoxian.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.lan.ShareToLan;

public class GuiExitGame {
    @SubscribeEvent
    public void onGuiOpenEvent(ScreenOpenEvent event) {
        Screen guiScreen = event.getScreen();
        if (guiScreen instanceof PauseScreen) {
            event.setScreen(new GuiInGameMenuModified());
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
            for (Widget widget : this.renderables) {
                if (widget instanceof Button button) {
                    if (button.getMessage().getString().equals(I18n.get("menu.returnToMenu"))) {
                        originalButton = button;
                        break;
                    }
                }
            }

            if (originalButton != null) {
                // 记录原按钮的参数
                int width = originalButton.getWidth();
                int height = originalButton.getHeight();
                int x = originalButton.x;
                int y = originalButton.y;

                // 删除原按钮
                this.renderables.remove(originalButton);
                this.removeWidget(originalButton);

                // 添加新按钮
                Button finalOriginalButton = originalButton;
                Button newButton = new Button(x, y, width, height, Component.nullToEmpty(I18n.get("menu.returnToMenu")), button -> {
                    ShareToLan.StopHttpAPIServer();
                    finalOriginalButton.onPress();
                });

                this.addRenderableWidget(newButton);
            }
        }
    }
}
