package org.xiaoxian.gui;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.util.ButtonUtil;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiWorldSelection) {
            event.setGui(new GuiWorldSelectionModified(new GuiMainMenu()));
        }
    }

    public static class GuiWorldSelectionModified extends GuiWorldSelection {
        public GuiWorldSelectionModified(GuiScreen parentScreen) {
            super(parentScreen);
        }

        @Override
        protected void initGui() {
            super.initGui();
            addButton(new ButtonUtil(5, 5, 100, 20, I18n.format("easylan.setting")) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    GuiWorldSelectionModified.this.mc.displayGuiScreen(new GuiEasyLanMain(GuiWorldSelectionModified.this));
                }
            });
        }
    }
}
