package org.xiaoxian.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        Screen guiScreen = event.getGui();
        if (guiScreen instanceof WorldSelectionScreen) {
            event.setGui(new GuiWorldSelectionModified(event.getGui()));
        }
    }

    public static class GuiWorldSelectionModified extends WorldSelectionScreen {

        public GuiWorldSelectionModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        protected void init() {
            this.addButton(new Button(5, 5, 100, 20, new StringTextComponent(I18n.format("easylan.setting")), (button) -> {
                assert GuiWorldSelectionModified.this.minecraft != null;
                GuiWorldSelectionModified.this.minecraft.displayGuiScreen(new GuiEasyLanMain(this));
            }));
            super.init();
        }
    }
}
