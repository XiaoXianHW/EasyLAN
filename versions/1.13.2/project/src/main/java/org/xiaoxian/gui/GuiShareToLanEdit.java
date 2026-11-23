package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.easylan.core.validation.ValidationRules;
import org.xiaoxian.easylan.forge.version.LanPublication;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.ChatUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import java.io.IOException;
import java.net.ServerSocket;

public class GuiShareToLanEdit {
    public static GuiTextField PortTextBox;
    public static String PortText = "";
    public static String PortWarningText = "";

    public static GuiTextField MaxPlayerBox;
    public static String MaxPlayerText = "";
    public static String MaxPlayerWarningText = "";

    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiShareToLan) {
            event.setGui(new GuiShareToLanModified(new GuiIngameMenu()));
        }
    }

    public static class GuiShareToLanModified extends GuiShareToLan {
        private final FontRenderer localFontRenderer = Minecraft.getInstance().fontRenderer;

        private boolean publishing;

        public GuiShareToLanModified(GuiScreen parentScreen) {
            super(parentScreen);
        }

        @Override
        protected void initGui() {
            super.initGui();

            PortTextBox = new TextBoxUtil(localFontRenderer, this.width / 2 - 155, this.height - 70, 145, 20, "");
            PortTextBox.setMaxStringLength(5);
            PortTextBox.setText(PortText);

            MaxPlayerBox = new TextBoxUtil(localFontRenderer, this.width / 2 + 5, this.height - 70, 145, 20, "");
            MaxPlayerBox.setMaxStringLength(6);
            MaxPlayerBox.setText(MaxPlayerText);

            GuiButton startButton = findButton();
            if (startButton != null) {
                startButton.enabled = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }

            GuiButton originalButton = startButton;
            if (originalButton != null) {
                int width = originalButton.getWidth();
                int height = originalButton.height;
                int x = originalButton.x;
                int y = originalButton.y;

                buttons.remove(originalButton);
                children.remove(originalButton);

                GuiButton newButton = addButton(new ButtonUtil(x, y, width, height, I18n.format("lanServer.start")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        startLan();
                    }
                });
                newButton.enabled = checkPortAndEnableButton(PortTextBox.getText())
                        && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }
        }

        private void startLan() {
            Minecraft minecraft = Minecraft.getInstance();
            IntegratedServer server = minecraft.getIntegratedServer();
            if (publishing || server == null || server.getPublic()) {
                return;
            }
            if (!checkPortAndEnableButton(PortTextBox.getText())
                    || !checkMaxPlayerAndEnableButton(MaxPlayerBox.getText())) {
                return;
            }

            publishing = true;
            try {
                GameType gameMode = LanPublication.selectedGameMode(this, GuiShareToLan.class, GameType.class);
                boolean commands = LanPublication.selectedCommands(this, GuiShareToLan.class);
                int port = LanPublication.publish(PortTextBox.getText(), gameMode, commands,
                        LanPublication::availablePort,
                        (mode, allowCommands, selectedPort) -> server.shareToLAN(mode, allowCommands, selectedPort));
                if (port < 0) {
                    ChatUtil.sendMsg(I18n.format("commands.publish.failed"));
                    return;
                }

                // Vanilla now owns the only listener and advertises this same port.
                EasyLAN.getRuntimeState().setLanPort(String.valueOf(server.getServerPort()));
                ChatUtil.sendMsg(I18n.format("commands.publish.started", server.getServerPort()));
                new ShareToLan().handleLanSetup();
                PortText = PortTextBox.getText();
                MaxPlayerText = MaxPlayerBox.getText();
                EasyLAN.CustomPort = PortText;
                EasyLAN.CustomMaxPlayer = MaxPlayerText;
                ConfigUtil.set("Port", PortText);
                ConfigUtil.set("MaxPlayer", MaxPlayerText);
                ConfigUtil.save();
            } catch (ReflectiveOperationException | IOException | IllegalArgumentException ex) {
                ChatUtil.sendMsg(I18n.format("commands.publish.failed"));
                System.err.println("[EasyLAN] LAN publication failed: " + ex);
            } finally {
                publishing = false;
                minecraft.displayGuiScreen(null);
            }
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);

            PortTextBox.drawTextField(mouseX, mouseY, partialTicks);
            MaxPlayerBox.drawTextField(mouseX, mouseY, partialTicks);

            drawString(localFontRenderer, I18n.format("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            drawString(localFontRenderer, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            drawString(localFontRenderer, I18n.format("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            drawString(localFontRenderer, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            PortTextBox.keyPressed(keyCode, scanCode, modifiers);
            MaxPlayerBox.keyPressed(keyCode, scanCode, modifiers);

            GuiButton startButton = findButton();
            if (startButton != null) {
                startButton.enabled = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }

            MaxPlayerText = MaxPlayerBox.getText();
            PortText = PortTextBox.getText();
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char typedChar, int keyCode) {
            PortTextBox.charTyped(typedChar, keyCode);
            MaxPlayerBox.charTyped(typedChar, keyCode);

            GuiButton startButton = findButton();
            if (startButton != null) {
                startButton.enabled = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }

            MaxPlayerText = MaxPlayerBox.getText();
            PortText = PortTextBox.getText();
            return super.charTyped(typedChar, keyCode);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            PortTextBox.mouseClicked(mouseX, mouseY, mouseButton);
            PortText = PortTextBox.getText();

            MaxPlayerBox.mouseClicked(mouseX, mouseY, mouseButton);
            MaxPlayerText = MaxPlayerBox.getText();
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private GuiButton findButton() {
            for (GuiButton button : buttons) {
                if (I18n.format("lanServer.start").equals(button.displayString)) {
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
                boolean isPortAvailable = ValidationRules.isValidPort(port) && isPortAvailable(port);
                PortWarningText = isPortAvailable ? "" : I18n.format("easylan.text.port.used");
                if (!ValidationRules.isValidPort(port)) {
                    PortWarningText = I18n.format("easylan.text.port.invalid");
                }
                return isPortAvailable;
            } catch (NumberFormatException e) {
                PortWarningText = I18n.format("easylan.text.port.invalid");
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
                if (!ValidationRules.isValidMaxPlayer(maxPlayer)) {
                    MaxPlayerWarningText = I18n.format("easylan.text.maxplayer.invalid");
                    return false;
                }
                MaxPlayerWarningText = "";
                return true;
            } catch (NumberFormatException e) {
                MaxPlayerWarningText = I18n.format("easylan.text.maxplayer.invalid");
                return false;
            }
        }

        private boolean isPortAvailable(int port) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
