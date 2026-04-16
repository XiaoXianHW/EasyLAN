package org.xiaoxian.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import org.xiaoxian.lan.ShareToLan;
import org.xiaoxian.util.ConfigUtil;

public class GuiShareToLanEdit {
    public static String PortText = "";
    public static String PortWarningText = "";
    public static String MaxPlayerText = "";
    public static String MaxPlayerWarningText = "";

    @SubscribeEvent
    public void onGuiOpenEvent(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof ShareToLanScreen) {
            event.setNewScreen(new GuiShareToLanModified(new PauseScreen(true)));
        }
    }

    public static class GuiShareToLanModified extends Screen {
        private final Screen lastScreen;
        private EditBox portTextBox;
        private EditBox maxPlayerTextBox;
        private Button startButton;
        private GameType gameMode = GameType.SURVIVAL;
        private boolean commands;
        private int publishPort;

        public GuiShareToLanModified(Screen lastScreen) {
            super(Component.translatable("lanServer.title"));
            this.lastScreen = lastScreen;
        }

        @Override
        protected void init() {
            IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
            if (integratedServer != null) {
                gameMode = integratedServer.getDefaultGameType();
                commands = integratedServer.getWorldData().isAllowCommands();
            }

            publishPort = HttpUtil.getAvailablePort();
            PortText = org.xiaoxian.EasyLAN.CustomPort;
            MaxPlayerText = org.xiaoxian.EasyLAN.CustomMaxPlayer;

            addRenderableWidget(createCycleButton(this.width / 2 - 155, 100, 150, 20));
            addRenderableWidget(createCommandsButton(this.width / 2 + 5, 100, 150, 20));

            portTextBox = new EditBox(this.font, this.width / 2 - 155, this.height - 70, 145, 20, Component.translatable("easylan.text.port"));
            portTextBox.setMaxLength(5);
            portTextBox.setValue(PortText);
            portTextBox.setHint(Component.literal(String.valueOf(publishPort)));
            portTextBox.setResponder(value -> {
                PortText = value;
                refreshWarnings();
            });
            addRenderableWidget(portTextBox);

            maxPlayerTextBox = new EditBox(this.font, this.width / 2 + 5, this.height - 70, 145, 20, Component.translatable("easylan.text.maxplayer"));
            maxPlayerTextBox.setMaxLength(6);
            maxPlayerTextBox.setValue(MaxPlayerText);
            maxPlayerTextBox.setResponder(value -> {
                MaxPlayerText = value;
                refreshWarnings();
            });
            addRenderableWidget(maxPlayerTextBox);

            startButton = Button.builder(Component.translatable("lanServer.start"), button -> startLan())
                    .bounds(this.width / 2 - 155, this.height - 28, 150, 20)
                    .build();
            addRenderableWidget(startButton);

            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                    .bounds(this.width / 2 + 5, this.height - 28, 150, 20)
                    .build());

            refreshWarnings();
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(this.lastScreen);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 0xFFFFFFFF);
            guiGraphics.drawCenteredString(this.font, Component.translatable("lanServer.otherPlayers"), this.width / 2, 82, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, I18n.get("easylan.text.port"), this.width / 2 - 155, this.height - 85, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, PortWarningText, this.width / 2 - 155, this.height - 45, 0xFFFF5555);
            guiGraphics.drawString(this.font, I18n.get("easylan.text.maxplayer"), this.width / 2 + 5, this.height - 85, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, MaxPlayerWarningText, this.width / 2 + 5, this.height - 45, 0xFFFF5555);
        }

        private Button createCycleButton(int x, int y, int width, int height) {
            return Button.builder(gameModeMessage(), button -> {
                GameType[] values = new GameType[] {
                        GameType.SURVIVAL,
                        GameType.SPECTATOR,
                        GameType.CREATIVE,
                        GameType.ADVENTURE
                };
                int index = 0;
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == gameMode) {
                        index = i;
                        break;
                    }
                }
                gameMode = values[(index + 1) % values.length];
                button.setMessage(gameModeMessage());
            }).bounds(x, y, width, height).build();
        }

        private Button createCommandsButton(int x, int y, int width, int height) {
            return Button.builder(commandsMessage(), button -> {
                commands = !commands;
                button.setMessage(commandsMessage());
            }).bounds(x, y, width, height).build();
        }

        private Component gameModeMessage() {
            return Component.literal(I18n.get("selectWorld.gameMode") + ": " + gameMode.getShortDisplayName().getString());
        }

        private Component commandsMessage() {
            return Component.literal(I18n.get("selectWorld.allowCommands") + ": " + (commands ? "ON" : "OFF"));
        }

        private void refreshWarnings() {
            boolean portValid = validatePort(portTextBox.getValue());
            boolean maxPlayerValid = validateMaxPlayers(maxPlayerTextBox.getValue());
            startButton.active = portValid && maxPlayerValid;
        }

        private boolean validatePort(String value) {
            PortWarningText = "";
            if (value == null || value.isEmpty()) {
                return true;
            }

            try {
                int port = Integer.parseInt(value);
                if (port < 100 || port > 65535) {
                    PortWarningText = I18n.get("easylan.text.port.invalid");
                    return false;
                }
                if (!HttpUtil.isPortAvailable(port)) {
                    PortWarningText = I18n.get("easylan.text.port.used");
                    return false;
                }
                return true;
            } catch (NumberFormatException exception) {
                PortWarningText = I18n.get("easylan.text.port.invalid");
                return false;
            }
        }

        private boolean validateMaxPlayers(String value) {
            MaxPlayerWarningText = "";
            if (value == null || value.isEmpty()) {
                return true;
            }

            try {
                int maxPlayers = Integer.parseInt(value);
                if (maxPlayers < 2 || maxPlayers > 500000) {
                    MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                    return false;
                }
                return true;
            } catch (NumberFormatException exception) {
                MaxPlayerWarningText = I18n.get("easylan.text.maxplayer.invalid");
                return false;
            }
        }

        private void startLan() {
            IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
            if (integratedServer == null) {
                return;
            }

            PortText = portTextBox.getValue();
            MaxPlayerText = maxPlayerTextBox.getValue();

            this.minecraft.setScreen(null);
            Component result;
            if (integratedServer.publishServer(gameMode, commands, publishPort)) {
                result = PublishCommand.getSuccessMessage(publishPort);
                org.xiaoxian.EasyLAN.CustomPort = PortText;
                org.xiaoxian.EasyLAN.CustomMaxPlayer = MaxPlayerText;
                ConfigUtil.save();
                new ShareToLan().handleLanSetup();
            } else {
                result = Component.translatable("commands.publish.failed");
            }

            this.minecraft.gui.getChat().addMessage(result);
            this.minecraft.updateTitle();
        }
    }
}
