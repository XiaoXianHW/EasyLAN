package org.xiaoxian.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.xiaoxian.easylan.fabric.version.VersionBridgeResolver;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
        private final Font font = Minecraft.getInstance().font;

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

            PortTextBox = new TextBoxUtil(font, this.width / 2 - 155, this.height - 70, 145, 20, "");
            PortTextBox.setMaxLength(5);
            PortTextBox.setValue(PortText);

            MaxPlayerBox = new TextBoxUtil(font, this.width / 2 + 5, this.height - 70, 145, 20, "");
            MaxPlayerBox.setMaxLength(6);
            MaxPlayerBox.setValue(MaxPlayerText);

            Button originalButton = findLanButton();
            if (originalButton != null) {
                int width = originalButton.getWidth();
                int height = originalButton.getHeight();
                int x = originalButton.x;
                int y = originalButton.y;

                this.buttons.remove(originalButton);
                this.children.remove(originalButton);

                Button newButton = new Button(x, y, width, height, new TextComponent(I18n.get("lanServer.start")), button -> {
                    syncTextState();
                    if (!publishSelectedPort()) {
                        return;
                    }
                    CustomPort = PortText;
                    CustomMaxPlayer = MaxPlayerText;
                    ConfigUtil.save();
                    new ShareToLan().handleLanSetup();
                });
                newButton.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
                this.addButton(newButton);
            }

            List<EditBox> originalTextFields = new ArrayList<>();
            for (Object child : this.children) {
                if (child instanceof EditBox) {
                    originalTextFields.add((EditBox) child);
                }
            }
            this.children.removeAll(originalTextFields);

            refreshLanButtonState();
        }

        @Override
        public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(matrixStack);
            drawCenteredString(matrixStack, font, this.title.getString(), this.width / 2, 50, 0xFFFFFF);
            drawCenteredString(matrixStack, font, I18n.get("lanServer.otherPlayers"), this.width / 2, 82, 0xFFFFFF);

            for (Object widget : this.buttons) {
                if (widget instanceof Button) {
                    ((Button) widget).render(matrixStack, mouseX, mouseY, partialTicks);
                }
            }

            PortTextBox.render(matrixStack, mouseX, mouseY, partialTicks);
            MaxPlayerBox.render(matrixStack, mouseX, mouseY, partialTicks);

            drawString(matrixStack, font, I18n.get("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, font, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            drawString(matrixStack, font, I18n.get("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, font, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);
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

        private boolean publishSelectedPort() {
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null || server.isPublished() || !checkPortAndEnableButton(PortText)) {
                return false;
            }

            // 1.16 has no vanilla port field. Keep its selected game mode and
            // commands, but give publishServer the requested port directly.
            final String mode;
            final boolean commands;
            try {
                mode = (String) readVanillaOption(String.class);
                commands = (Boolean) readVanillaOption(Boolean.TYPE);
            } catch (ReflectiveOperationException exception) {
                PortWarningText = I18n.get("easylan.chat.CtPortError");
                return false;
            }
            int selectedPort = PortText.isEmpty() ? HttpUtil.getAvailablePort() : Integer.parseInt(PortText);
            this.minecraft.setScreen(null);
            boolean published = server.publishServer(GameType.byName(mode), commands, selectedPort)
                    && server.isPublished() && server.getPort() == selectedPort;
            Component result = published
                    ? new TranslatableComponent("commands.publish.started", server.getPort())
                    : new TranslatableComponent("commands.publish.failed");
            this.minecraft.gui.getChat().addMessage(result);
            this.minecraft.updateTitle();
            return published;
        }

        private Object readVanillaOption(Class<?> optionType) throws ReflectiveOperationException {
            Field selected = null;
            // Restrict this lookup to vanilla's declared instance fields. Their
            // types are stable in named and intermediary production mappings.
            for (Field field : ShareToLanScreen.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && field.getType() == optionType) {
                    if (selected != null) {
                        throw new NoSuchFieldException("Ambiguous LAN option: " + optionType.getName());
                    }
                    selected = field;
                }
            }
            if (selected == null) {
                throw new NoSuchFieldException("Missing LAN option: " + optionType.getName());
            }
            selected.setAccessible(true);
            return selected.get(this);
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
            for (Object widget : this.buttons) {
                if (widget instanceof Button) {
                    Button button = (Button) widget;
                    if (button.getMessage().getString().equals(I18n.get("lanServer.start"))) {
                        return button;
                    }
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
