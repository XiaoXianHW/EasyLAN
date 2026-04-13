package org.xiaoxian.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;

import static org.xiaoxian.EasyLAN.CustomMaxPlayer;
import static org.xiaoxian.EasyLAN.CustomPort;

public class GuiShareToLanEdit {
    public static EditBox PortTextBox;
    public static String PortText = "";
    public static String PortWarningText = "";

    public static EditBox MaxPlayerBox;
    public static String MaxPlayerText = "";
    public static String MaxPlayerWarningText = "";

    @SubscribeEvent
    public void onGuiOpenEvent(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof ShareToLanScreen) {
            event.setNewScreen(new GuiShareToLanModified(new PauseScreen(true)));
        }
    }

    public static class GuiShareToLanModified extends ShareToLanScreen {
        private final Font fontRenderer = Minecraft.getInstance().font;

        public GuiShareToLanModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        public void init() {
            super.init();

            PortText = CustomPort;
            MaxPlayerText = CustomMaxPlayer;

            PortTextBox = new TextBoxUtil(fontRenderer, this.width / 2 - 155, this.height - 70, 145, 20, "");
            PortTextBox.setMaxLength(5);
            PortTextBox.setValue(PortText);

            MaxPlayerBox = new TextBoxUtil(fontRenderer, this.width / 2 + 5, this.height - 70, 145, 20, "");
            MaxPlayerBox.setMaxLength(6);
            MaxPlayerBox.setValue(MaxPlayerText);

            refreshLanButtonState();

            Button originalButton = null;
            for (Widget widget : this.renderables) {
                if (widget instanceof Button button && button.getMessage().getString().equals(I18n.get("lanServer.start"))) {
                    originalButton = button;
                    break;
                }
            }

            if (originalButton != null) {
                int width = originalButton.getWidth();
                int height = originalButton.getHeight();
                int x = originalButton.x;
                int y = originalButton.y;

                this.renderables.remove(originalButton);
                this.removeWidget(originalButton);

                Button finalOriginalButton = originalButton;
                Button newButton = new Button(x, y, width, height, Component.nullToEmpty(I18n.get("lanServer.start")), button -> {
                    syncTextState();
                    finalOriginalButton.onPress();
                    CustomPort = PortText;
                    CustomMaxPlayer = MaxPlayerText;
                    ConfigUtil.save();
                    new ShareToLan().handleLanSetup();
                });

                this.addRenderableWidget(newButton);
            }
        }

        @Override
        public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.render(matrixStack, mouseX, mouseY, partialTicks);

            PortTextBox.render(matrixStack, mouseX, mouseY, partialTicks);
            MaxPlayerBox.render(matrixStack, mouseX, mouseY, partialTicks);

            drawString(matrixStack, Minecraft.getInstance().font, I18n.get("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, fontRenderer, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            drawString(matrixStack, fontRenderer, I18n.get("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, fontRenderer, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            PortTextBox.keyPressed(keyCode, scanCode, modifiers);
            MaxPlayerBox.keyPressed(keyCode, scanCode, modifiers);
            refreshLanButtonState();
            syncTextState();
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char typedChar, int keyCode) {
            PortTextBox.charTyped(typedChar, keyCode);
            MaxPlayerBox.charTyped(typedChar, keyCode);
            refreshLanButtonState();
            syncTextState();
            return super.charTyped(typedChar, keyCode);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            PortTextBox.mouseClicked(mouseX, mouseY, mouseButton);
            MaxPlayerBox.mouseClicked(mouseX, mouseY, mouseButton);
            syncTextState();
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private void syncTextState() {
            PortText = PortTextBox.getValue();
            MaxPlayerText = MaxPlayerBox.getValue();
        }

        private void refreshLanButtonState() {
            Button button = (Button) findButton();
            if (button != null) {
                button.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }
        }

        private Widget findButton() {
            for (Widget widget : this.renderables) {
                if (widget instanceof Button button && button.getMessage().getString().equals(I18n.get("lanServer.start"))) {
                    return button;
                }
            }
            return null;
        }

        private boolean checkPortAndEnableButton(String portText) {
            if (portText.isEmpty()) {
                PortWarningText = "";
                return true;
            }

            try {
                int port = Integer.parseInt(portText);
                boolean isPortAvailable = port >= 100 && port <= 65535 && isPortAvailable(port);
                PortWarningText = isPortAvailable ? "" : I18n.get("easylan.text.port.used");

                if (!(port >= 100 && port <= 65535)) {
                    PortWarningText = I18n.get("easylan.text.port.invalid");
                }

                return isPortAvailable;
            } catch (NumberFormatException ex) {
                PortWarningText = I18n.get("easylan.text.port.invalid");
                return false;
            }
        }

        private boolean checkMaxPlayerAndEnableButton(String maxPlayerText) {
            if (maxPlayerText.isEmpty()) {
                MaxPlayerWarningText = "";
                return true;
            }

            try {
                int maxPlayer = Integer.parseInt(maxPlayerText);
                if (!(maxPlayer >= 2 && maxPlayer <= 500000)) {
                    MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                    return false;
                }
                MaxPlayerWarningText = "";
                return true;
            } catch (NumberFormatException ex) {
                MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                return false;
            }
        }

        public boolean isPortAvailable(int port) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
    }
}
