package org.xiaoxian.gui;

import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.lan.ShareToLan;

public class GuiExitGame {
    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        Screen guiScreen = event.getGui();
        if (guiScreen instanceof IngameMenuScreen) {
            event.setGui(new GuiExitGame.GuiInGameMenuModified());
        }
    }

    public static class GuiInGameMenuModified extends IngameMenuScreen {

        public GuiInGameMenuModified() {
            super(true);
        }

        @Override
        protected void init() {
            super.init();

            Button originalButton = null;
            for (Widget widget : this.buttons) {
                if (widget instanceof Button) {
                    Button button = (Button) widget;
                    if (button.getMessage().getString().equals(I18n.format("menu.returnToMenu"))) {
                        originalButton = button;
                        break;
                    }
                }
            }

            if (originalButton != null) {
                // 记录原按钮的参数
                int width = originalButton.getWidth();
                int height = originalButton.getHeightRealms();
                int x = originalButton.x;
                int y = originalButton.y;

                // 删除原按钮
                this.buttons.remove(originalButton);
                this.children.remove(originalButton);

                // 添加新按钮
                Button finalOriginalButton = originalButton;
                Button newButton = new Button(x, y, width, height, new StringTextComponent(I18n.format("menu.returnToMenu")), button -> {
                    ShareToLan.StopHttpAPIServer();
                    finalOriginalButton.onPress();
                });

                this.addButton(newButton);
            }
        }
    }
}
