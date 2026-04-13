package org.xiaoxian.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
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
        if (event.gui instanceof GuiShareToLan) {
            event.gui = new GuiShareToLanModified(new GuiIngameMenu());
        }
    }

    public static class GuiShareToLanModified extends GuiShareToLan {

        public GuiShareToLanModified(GuiScreen parentScreen) {
            super(parentScreen);
        }

        @Override
        public void initGui() {
            super.initGui();

            PortTextBox = new TextBoxUtil(4, fontRendererObj, this.width / 2 - 155, this.height - 70, 145, 20);
            PortTextBox.setMaxStringLength(5);
            PortTextBox.setText(PortText);

            MaxPlayerBox = new TextBoxUtil(5, fontRendererObj, this.width / 2 + 5, this.height - 70, 145, 20);
            MaxPlayerBox.setMaxStringLength(6);
            MaxPlayerBox.setText(MaxPlayerText);

            GuiButton button101 = findButton();
            if (button101 != null) {
                button101.enabled = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);

            PortTextBox.drawTextBox();
            MaxPlayerBox.drawTextBox();

            drawString(fontRendererObj, I18n.format("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            drawString(fontRendererObj, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            drawString(fontRendererObj, I18n.format("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            drawString(fontRendererObj, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);

        }

        @Override
        protected void actionPerformed(@Nonnull GuiButton button) throws IOException{
            super.actionPerformed(button);
            if (button.id == 101) {
                ConfigUtil.set("Port", PortText);
                ConfigUtil.set("MaxPlayer", MaxPlayerText);
                ConfigUtil.save();
            }
            GuiButton button101 = findButton();
            if (button101 != null) {
                button101.enabled = checkPortAndEnableButton(PortTextBox.getText());
            }
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) {
            PortTextBox.textboxKeyTyped(typedChar, keyCode);
            MaxPlayerBox.textboxKeyTyped(typedChar, keyCode);

            String previousText = PortTextBox.getText();
            String previousMaxPlayerText = MaxPlayerBox.getText();

            if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
                if (Character.isDigit(typedChar)) {
                    String newPortText = PortTextBox.getText();
                    String newMaxPlayerText = MaxPlayerBox.getText();
                    try {
                        int newPort = Integer.parseInt(newPortText);
                        int newMaxPlayer = Integer.parseInt(newMaxPlayerText);

                        if (!(newPort >= 100 && newPort <= 65535)) {
                            PortTextBox.setText(previousText);
                        }

                        if (!(newMaxPlayer >= 2 && newMaxPlayer <= 500000)) {
                            MaxPlayerBox.setText(previousMaxPlayerText);
                        }
                    } catch (NumberFormatException e) {
                        PortTextBox.setText(previousText);
                        MaxPlayerBox.setText(previousMaxPlayerText);
                    }
                } else if (keyCode == Keyboard.KEY_ESCAPE) {
                    mc.displayGuiScreen(null);
                    if (mc.currentScreen == null) mc.setIngameFocus();
                }
            }

            GuiButton button101 = findButton();
            if (button101 != null) {
                button101.enabled = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }

            MaxPlayerText = MaxPlayerBox.getText();
            PortText = PortTextBox.getText();
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
            PortTextBox.mouseClicked(mouseX, mouseY, mouseButton);
            PortText = PortTextBox.getText();

            MaxPlayerBox.mouseClicked(mouseX, mouseY, mouseButton);
            MaxPlayerText = MaxPlayerBox.getText();

            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private GuiButton findButton() {
            for (GuiButton button : buttonList) {
                if (button.id == 101) {
                    return button;
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
                    boolean isPortAvailable = port >= 100 && port <= 65535 && isPortAvailable(port);
                    PortWarningText = isPortAvailable ? "" : I18n.format("easylan.text.port.used");

                    if (!(port >= 100 && port <= 65535)) {
                        PortWarningText = I18n.format("easylan.text.port.invalid");
                    }

                    return isPortAvailable;
                } catch (NumberFormatException e) {
                    PortWarningText = I18n.format("easylan.text.port.invalid");
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
                    if (!(maxPlayer >= 2 && maxPlayer <= 500000)) {
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
