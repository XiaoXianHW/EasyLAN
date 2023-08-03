package org.xiaoxian.gui;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;

public class GuiWorldSelectionEdit {
    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        GuiScreen guiScreen = event.gui;
        if (guiScreen instanceof GuiSelectWorld) {
            event.gui = new GuiWorldSelectionModified(guiScreen);
        }
    }

    public static class GuiWorldSelectionModified extends GuiSelectWorld {

        Minecraft mc = Minecraft.getMinecraft();
        public GuiWorldSelectionModified(GuiScreen parentScreen) {
            super(new GuiMainMenu());
        }

        @Override
        public void initGui() {
            buttonList.add(new GuiButton(89, 5, 5, 100, 20, I18n.format("easylan.setting")));
            super.initGui();
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            if (button.id == 89) {
                mc.displayGuiScreen(new GuiEasyLanMain(this));
            }
            super.actionPerformed(button);
        }
    }
}
