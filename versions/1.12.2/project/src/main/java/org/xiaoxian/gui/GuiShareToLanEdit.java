package org.xiaoxian.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.xiaoxian.easylan.core.validation.ValidationRules;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;

import static org.xiaoxian.EasyLAN.CustomMaxPlayer;
import static org.xiaoxian.EasyLAN.CustomPort;

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
        public GuiShareToLanModified(GuiScreen parentScreen) {
            super(parentScreen);
        }

        @Override
        public void initGui() {
            super.initGui();

            PortText = CustomPort;
            MaxPlayerText = CustomMaxPlayer;

            PortTextBox = new TextBoxUtil(4, fontRenderer, this.width / 2 - 155, this.height - 70, 145, 20);
            PortTextBox.setMaxStringLength(5);
            PortTextBox.setText(PortText);

            MaxPlayerBox = new TextBoxUtil(5, fontRenderer, this.width / 2 + 5, this.height - 70, 145, 20);
            MaxPlayerBox.setMaxStringLength(6);
            MaxPlayerBox.setText(MaxPlayerText);

            refreshLanButtonState();
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);

            PortTextBox.drawTextBox();
            MaxPlayerBox.drawTextBox();

            drawString(fontRenderer, I18n.format("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            drawString(fontRenderer, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            drawString(fontRenderer, I18n.format("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            drawString(fontRenderer, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);
        }

        @Override
        protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
            if (button.id == 101) {
                syncTextState();
            }

            super.actionPerformed(button);

            if (button.id == 101) {
                CustomPort = PortText;
                CustomMaxPlayer = MaxPlayerText;
                ConfigUtil.save();
                new ShareToLan().handleLanSetup();
            }

            refreshLanButtonState();
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                mc.displayGuiScreen(null);
                if (mc.currentScreen == null) {
                    mc.setIngameFocus();
                }
                return;
            }

            PortTextBox.textboxKeyTyped(typedChar, keyCode);
            MaxPlayerBox.textboxKeyTyped(typedChar, keyCode);

            sanitizeNumericText(PortTextBox);
            sanitizeNumericText(MaxPlayerBox);
            syncTextState();
            refreshLanButtonState();
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            PortTextBox.mouseClicked(mouseX, mouseY, mouseButton);
            MaxPlayerBox.mouseClicked(mouseX, mouseY, mouseButton);
            syncTextState();
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        private void sanitizeNumericText(GuiTextField textField) {
            String sanitized = textField.getText().replaceAll("\\D", "");
            if (!sanitized.equals(textField.getText())) {
                textField.setText(sanitized);
            }
        }

        private void syncTextState() {
            PortText = PortTextBox.getText();
            MaxPlayerText = MaxPlayerBox.getText();
        }

        private void refreshLanButtonState() {
            GuiButton button = findLanButton();
            if (button != null) {
                button.enabled = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }
        }

        private GuiButton findLanButton() {
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
            }

            try {
                int port = Integer.parseInt(portText);
                if (!ValidationRules.isValidPort(port)) {
                    PortWarningText = I18n.format("easylan.text.port.invalid");
                    return false;
                }

                boolean available = isPortAvailable(port);
                PortWarningText = available ? "" : I18n.format("easylan.text.port.used");
                return available;
            } catch (NumberFormatException ex) {
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
                boolean valid = ValidationRules.isValidMaxPlayer(maxPlayer);
                MaxPlayerWarningText = valid ? "" : I18n.format("easylan.text.maxplayer.invalid");
                return valid;
            } catch (NumberFormatException ex) {
                MaxPlayerWarningText = I18n.format("easylan.text.maxplayer.invalid");
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
