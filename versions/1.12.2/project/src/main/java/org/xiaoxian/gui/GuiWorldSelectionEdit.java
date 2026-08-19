package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;

public class GuiWorldSelectionEdit {
    private static final int SETTING_BUTTON_ID = 89;

    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiWorldSelection) {
            event.setGui(new GuiWorldSelectionModified(new GuiMainMenu()));
        }
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        // GuiCreateWorld opens directly when the player has no world yet, so it needs the button too.
        if (event.getGui() instanceof GuiCreateWorld) {
            event.getButtonList().add(new GuiButton(SETTING_BUTTON_ID, 5, 5, 100, 20,
                    I18n.format("easylan.setting")));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getGui() instanceof GuiCreateWorld && event.getButton().id == SETTING_BUTTON_ID) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEasyLanMain(event.getGui()));
        }
    }

    public static class GuiWorldSelectionModified extends GuiWorldSelection {

        public GuiWorldSelectionModified(GuiScreen parentScreen) {
            super(parentScreen);
        }

        @Override
        public void initGui() {
            super.initGui();
            buttonList.add(new GuiButton(89, 5, 5, 100, 20, I18n.format("easylan.setting")));
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException {
            if (button.id == 89) {
                mc.displayGuiScreen(new GuiEasyLanMain(this));
            } else {
                super.actionPerformed(button);
            }
        }
    }
}
