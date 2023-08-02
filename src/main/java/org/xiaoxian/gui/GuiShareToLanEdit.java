package org.xiaoxian.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ShareToLanScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;

public class GuiShareToLanEdit {

    public static TextFieldWidget PortTextBox;
    public static String PortText = "";
    public static String PortWarningText = "";

    public static TextFieldWidget MaxPlayerBox;
    public static String MaxPlayerText = "";
    public static String MaxPlayerWarningText = "";

    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        Screen guiScreen = event.getGui();
        if (guiScreen instanceof ShareToLanScreen) {
            event.setGui(new GuiShareToLanEdit.GuiShareToLanModified(event.getGui()));
        }
    }

    public static class GuiShareToLanModified extends ShareToLanScreen {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

        public GuiShareToLanModified(Screen parentScreen) {
            super(parentScreen);
        }

        @Override
        public void init() {
            super.init();

            PortTextBox = new TextBoxUtil(fontRenderer, this.width / 2 - 155, this.height - 70, 145, 20, "");
            PortTextBox.setMaxStringLength(5);
            PortTextBox.setText(PortText);

            MaxPlayerBox = new TextBoxUtil(fontRenderer, this.width / 2 + 5, this.height - 70, 145, 20, "");
            MaxPlayerBox.setMaxStringLength(6);
            MaxPlayerBox.setText(MaxPlayerText);

            Widget button101 = findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }

            Button originalButton = null;
            for (Widget widget : this.buttons) {
                if (widget instanceof Button) {
                    Button button = (Button) widget;
                    if (button.getMessage().getString().equals(I18n.format("lanServer.start"))) {
                        originalButton = button;
                        break;
                    }
                }
            }

            if (originalButton != null) {
                // 记录原按钮的参数
                int width = originalButton.getWidth();
                int height = originalButton.getHeightRealms();
                int x = originalButton.x;
                int y = originalButton.y;

                // 删除原按钮
                this.buttons.remove(originalButton);
                this.children.remove(originalButton);

                // 添加新按钮
                Button finalOriginalButton = originalButton;
                Button newButton = new Button(x, y, width, height, new StringTextComponent(I18n.format("lanServer.start")), button -> {
                    ShareToLan.NewShareToLAN();
                    finalOriginalButton.onPress();
                });

                this.addButton(newButton);
            }
        }

        @Override
        public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.render(matrixStack, mouseX, mouseY, partialTicks);

            PortTextBox.render(matrixStack, mouseX,mouseY,partialTicks);
            MaxPlayerBox.render(matrixStack, mouseX,mouseY,partialTicks);

            drawString(matrixStack, Minecraft.getInstance().fontRenderer, I18n.format("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, fontRenderer, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFF0000);

            drawString(matrixStack, fontRenderer, I18n.format("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFF);
            drawString(matrixStack, fontRenderer, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFF0000);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            PortTextBox.keyPressed(keyCode, scanCode, modifiers);
            MaxPlayerBox.keyPressed(keyCode, scanCode, modifiers);

            Widget button101 = findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
            }

            MaxPlayerText = MaxPlayerBox.getText();
            PortText = PortTextBox.getText();

            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char typedChar, int keyCode) {
            PortTextBox.charTyped(typedChar, keyCode);
            MaxPlayerBox.charTyped(typedChar, keyCode);

            String previousText = PortTextBox.getText();
            String previousMaxPlayerText = MaxPlayerBox.getText();

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
            }

            Widget button101 = findButton();
            if (button101 != null) {
                button101.active = checkPortAndEnableButton(PortTextBox.getText()) && checkMaxPlayerAndEnableButton(MaxPlayerBox.getText());
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

        private Widget findButton() {
            for (Widget button : buttons) {
                if (button.getMessage().getString().equals(I18n.format("lanServer.start"))) {
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
