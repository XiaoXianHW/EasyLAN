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
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.EasyLAN;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.xiaoxian.easylan.forge.version.LanPublication;
import org.xiaoxian.util.ChatUtil;
import org.xiaoxian.easylan.core.validation.ValidationRules;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;

public class GuiShareToLanEdit {

    public static EditBox PortTextBox;
    public static String PortText = "";
    public static String PortWarningText = "";

    public static EditBox MaxPlayerBox;
    public static String MaxPlayerText = "";
    public static String MaxPlayerWarningText = "";

    @SubscribeEvent
    public void onGuiOpenEvent(ScreenOpenEvent event) {
        if (event.getScreen() instanceof ShareToLanScreen) {
            event.setScreen(new GuiShareToLanModified(new PauseScreen(true)));
        }
    }

    public static class GuiShareToLanModified extends ShareToLanScreen {
        private boolean publishing;
        Font fontRenderer = Minecraft.getInstance().font;

        public GuiShareToLanModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        public void init() {
            super.init();

            PortTextBox = new TextBoxUtil(fontRenderer, this.width / 2 - 155, this.height - 70, 145, 20, "");
            PortTextBox.setMaxLength(5);
            PortTextBox.setValue(PortText);

            MaxPlayerBox = new TextBoxUtil(fontRenderer, this.width / 2 + 5, this.height - 70, 145, 20, "");
            MaxPlayerBox.setMaxLength(6);
            MaxPlayerBox.setValue(MaxPlayerText);

            Button button101 = (Button) findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }

            Button originalButton = null;
            for (Widget widget : this.renderables) {
                if (widget instanceof Button button) {
                    if (button.getMessage().getString().equals(I18n.get("lanServer.start"))) {
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
                Button newButton = new Button(x, y, width, height, Component.nullToEmpty(I18n.get("lanServer.start")), button -> {
                    startLan();
                });

                this.addRenderableWidget(newButton);
                newButton.active = checkPortAndEnableButton(PortTextBox.getValue())
                        && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }
        }

        private void startLan() {
            Minecraft minecraft = Minecraft.getInstance();
            IntegratedServer server = minecraft.getSingleplayerServer();
            if (publishing || server == null || server.isPublished()) {
                return;
            }
            if (!checkPortAndEnableButton(PortTextBox.getValue())
                    || !checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue())) {
                return;
            }

            publishing = true;
            try {
                GameType gameMode = LanPublication.selectedGameMode(this, ShareToLanScreen.class, GameType.class);
                boolean commands = LanPublication.selectedCommands(this, ShareToLanScreen.class);
                int port = LanPublication.publish(PortTextBox.getValue(), gameMode, commands,
                        LanPublication::availablePort,
                        (mode, allowCommands, selectedPort) -> server.publishServer(mode, allowCommands, selectedPort));
                if (port < 0) {
                    ChatUtil.sendMsg(I18n.get("commands.publish.failed"));
                    return;
                }

                // Vanilla now owns the only listener and advertises this same port.
                EasyLAN.getRuntimeState().setLanPort(String.valueOf(server.getPort()));
                ChatUtil.sendMsg(I18n.get("commands.publish.started", server.getPort()));
                new ShareToLan().handleLanSetup();
                PortText = PortTextBox.getValue();
                MaxPlayerText = MaxPlayerBox.getValue();
                EasyLAN.CustomPort = PortText;
                EasyLAN.CustomMaxPlayer = MaxPlayerText;
                ConfigUtil.set("Port", PortText);
                ConfigUtil.set("MaxPlayer", MaxPlayerText);
                ConfigUtil.save();
                minecraft.setScreen(null);
                minecraft.updateTitle();
            } catch (ReflectiveOperationException | IOException | IllegalArgumentException ex) {
                ChatUtil.sendMsg(I18n.get("commands.publish.failed"));
                System.err.println("[EasyLAN] LAN publication failed: " + ex);
            } finally {
                publishing = false;
            }
        }

        @Override
        public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.render(matrixStack, mouseX, mouseY, partialTicks);

            PortTextBox.render(matrixStack, mouseX,mouseY,partialTicks);
            MaxPlayerBox.render(matrixStack, mouseX,mouseY,partialTicks);

            drawString(matrixStack, Minecraft.getInstance().font, I18n.get("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, fontRenderer, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            drawString(matrixStack, fontRenderer, I18n.get("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, fontRenderer, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            PortTextBox.keyPressed(keyCode, scanCode, modifiers);
            MaxPlayerBox.keyPressed(keyCode, scanCode, modifiers);

            Button button101 = (Button) findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }

            MaxPlayerText = MaxPlayerBox.getValue();
            PortText = PortTextBox.getValue();

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char typedChar, int keyCode) {
            PortTextBox.charTyped(typedChar, keyCode);
            MaxPlayerBox.charTyped(typedChar, keyCode);

            String previousText = PortTextBox.getValue();
            String previousMaxPlayerText = MaxPlayerBox.getValue();

            if (Character.isDigit(typedChar)) {
                String newPortText = PortTextBox.getValue();
                String newMaxPlayerText = MaxPlayerBox.getValue();
                try {
                    int newPort = Integer.parseInt(newPortText);
                    int newMaxPlayer = Integer.parseInt(newMaxPlayerText);

                    if (!(newPort >= 100 && newPort <= 65535)) {
                        PortTextBox.setValue(previousText);
                    }

                    if (!(newMaxPlayer >= 2 && newMaxPlayer <= 500000)) {
                        MaxPlayerBox.setValue(previousMaxPlayerText);
                    }
                } catch (NumberFormatException e) {
                    PortTextBox.setValue(previousText);
                    MaxPlayerBox.setValue(previousMaxPlayerText);
                }
            }

            Button button101 = (Button) findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getValue()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getValue());
            }

            MaxPlayerText = MaxPlayerBox.getValue();
            PortText = PortTextBox.getValue();

            return super.charTyped(typedChar, keyCode);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            PortTextBox.mouseClicked(mouseX, mouseY, mouseButton);
            PortText = PortTextBox.getValue();

            MaxPlayerBox.mouseClicked(mouseX, mouseY, mouseButton);
            MaxPlayerText = MaxPlayerBox.getValue();

            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private Widget findButton() {
            for (Widget widget : this.renderables) {
                if (widget instanceof Button button) {
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
            } else {
                try {
                    int port = Integer.parseInt(portText);
                    boolean isPortAvailable = ValidationRules.isValidPort(port) && isPortAvailable(port);
                    PortWarningText = isPortAvailable ? "" : I18n.get("easylan.text.port.used");

                    if (!ValidationRules.isValidPort(port)) {
                        PortWarningText = I18n.get("easylan.text.port.invalid");
                    }

                    return isPortAvailable;
                } catch (NumberFormatException e) {
                    PortWarningText = I18n.get("easylan.text.port.invalid");
                    return false;
                }
            }
        }

        private boolean checkMaxPlayerAndEnableButton(String maxPlayerText) {
            if (maxPlayerText.isEmpty()) {
                MaxPlayerWarningText = "";
                return true;
            } else {
                try {
                    int maxPlayer = Integer.parseInt(maxPlayerText);
                    if (!ValidationRules.isValidMaxPlayer(maxPlayer)) {
                        MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                        return false;
                    }
                    MaxPlayerWarningText = "";
                    return true;
                } catch (NumberFormatException e) {
                    MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                    return false;
                }
            }
        }

        public boolean isPortAvailable(int port) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
