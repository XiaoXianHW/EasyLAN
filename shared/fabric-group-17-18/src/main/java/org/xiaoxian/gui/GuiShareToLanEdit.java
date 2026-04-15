package org.xiaoxian.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.xiaoxian.easylan.fabric.version.VersionBridgeResolver;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.xiaoxian.EasyLAN.CustomMaxPlayer;
import static org.xiaoxian.EasyLAN.CustomPort;

public class GuiShareToLanEdit {
    public static EditBox PortTextBox;
    public static String PortText = "";
    public static String PortWarningText = "";

    public static EditBox MaxPlayerBox;
    public static String MaxPlayerText = "";
    public static String MaxPlayerWarningText = "";

    public static void maybeReplace(Minecraft minecraft, Screen screen) {
        if (screen instanceof ShareToLanScreen && !(screen instanceof GuiShareToLanModified)) {
            Screen parentScreen = VersionBridgeResolver.get().resolveShareToLanParent(screen);
            minecraft.setScreen(new GuiShareToLanModified(parentScreen));
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
            PortWarningText = "";
            MaxPlayerWarningText = "";

            PortTextBox = new TextBoxUtil(fontRenderer, this.width / 2 - 155, this.height - 70, 145, 20, "");
            PortTextBox.setMaxLength(5);
            PortTextBox.setValue(PortText);

            MaxPlayerBox = new TextBoxUtil(fontRenderer, this.width / 2 + 5, this.height - 70, 145, 20, "");
            MaxPlayerBox.setMaxLength(6);
            MaxPlayerBox.setValue(MaxPlayerText);

            Button originalButton = findLanButton();
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
                newButton.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());

                this.addRenderableWidget(newButton);
            }

            List<EditBox> targetEditBoxes = new ArrayList<>();
            for (Renderable widget : this.renderables) {
                if (widget instanceof EditBox editBox) {
                    targetEditBoxes.add(editBox);
                }
            }

            for (EditBox targetEditBox : targetEditBoxes) {
                this.renderables.remove(targetEditBox);
                this.removeWidget(targetEditBox);
            }

            refreshLanButtonState();
        }

        @Override
        public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(matrixStack);
            drawCenteredString(matrixStack, fontRenderer, this.title, this.width / 2, 50, 0xFFFFFF);
            drawCenteredString(matrixStack, fontRenderer, Component.nullToEmpty(I18n.get("lanServer.otherPlayers")), this.width / 2, 82, 0xFFFFFF);

            for (Renderable widget : this.renderables) {
                widget.render(matrixStack, mouseX, mouseY, partialTicks);
            }

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
            Button button = findLanButton();
            if (button != null) {
                button.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }
        }

        private Button findLanButton() {
            for (Renderable widget : this.renderables) {
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

        private boolean isPortAvailable(int port) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
    }
}
