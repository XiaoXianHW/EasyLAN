package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.CheckBoxButtonUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import java.awt.Color;

import static org.xiaoxian.EasyLAN.BanCommands;
import static org.xiaoxian.EasyLAN.HttpAPI;
import static org.xiaoxian.EasyLAN.LanOutput;
import static org.xiaoxian.EasyLAN.OpCommands;
import static org.xiaoxian.EasyLAN.SaveCommands;
import static org.xiaoxian.EasyLAN.allowFlight;
import static org.xiaoxian.EasyLAN.allowPVP;
import static org.xiaoxian.EasyLAN.motd;
import static org.xiaoxian.EasyLAN.onlineMode;
import static org.xiaoxian.EasyLAN.spawnAnimals;
import static org.xiaoxian.EasyLAN.spawnNPCs;
import static org.xiaoxian.EasyLAN.whiteList;

public class GuiEasyLanMain extends Screen {
    private EditBox motdTextBox;
    private String motdText = motd;
    private boolean mobSpawningEnabled = spawnAnimals && spawnNPCs;
    private final Font fontRenderer = Minecraft.getInstance().font;
    private final Screen parentScreen;

    public GuiEasyLanMain(Screen parentScreen) {
        super(Component.translatable("easylan.setting"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        clearWidgets();
        mobSpawningEnabled = spawnAnimals && spawnNPCs;

        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 + 70, this.height - 25, 100, 20, text("easylan.back", "Back"))) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                Minecraft.getInstance().setScreen(parentScreen);
            }
        });
        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 - 50, this.height - 25, 100, 20, text("easylan.load", "Load Config"))) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                ConfigUtil.load();
                minecraft.setScreen(new GuiEasyLanMain(parentScreen));
            }
        });
        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 - 170, this.height - 25, 100, 20, text("easylan.save", "Save Config"))) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                saveConfig();
            }
        });

        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 55, allowPVP, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                allowPVP = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 80, onlineMode, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                onlineMode = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 118, mobSpawningEnabled, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                mobSpawningEnabled = this.isChecked();
                spawnAnimals = mobSpawningEnabled;
                spawnNPCs = mobSpawningEnabled;
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 144, allowFlight, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                allowFlight = this.isChecked();
            }
        });

        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 55, whiteList, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                whiteList = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 80, BanCommands, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                BanCommands = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 105, OpCommands, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                OpCommands = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 130, SaveCommands, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                SaveCommands = this.isChecked();
            }
        });

        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 145, 55, HttpAPI, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                HttpAPI = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 145, 80, LanOutput, 20, 20) {
            @Override
            public void onPress(InputWithModifiers inputWithModifiers) {
                super.onPress(inputWithModifiers);
                LanOutput = this.isChecked();
            }
        });

        motdTextBox = new TextBoxUtil(fontRenderer, this.width / 2 - 70, 185, 230, 20, "");
        motdTextBox.setMaxLength(100);
        motdTextBox.setValue(motdText);
        motdTextBox.setResponder(text -> motdText = text);
        addRenderableWidget(motdTextBox);
    }

    @Override
    public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        matrixStack.drawCenteredString(fontRenderer, text("easylan.setting", "EasyLAN Setting"), this.width / 2, 15, Color.WHITE.getRGB());

        matrixStack.drawString(fontRenderer, text("easylan.text.setting1", "Basic Settings"), this.width / 2 - 165, 35, 0xFF33CCFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.pvp", "Allow PVP"), this.width / 2 - 165, 60, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.onlineMode", "Online Mode"), this.width / 2 - 165, 85, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.spawnAnimals", "Spawn Animals"), this.width / 2 - 165, 110, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.spawnNPCs", "Spawn NPCs"), this.width / 2 - 165, 125, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.allowFlight", "Allow Flight"), this.width / 2 - 165, 150, 0xFFFFFFFF);

        matrixStack.drawString(fontRenderer, text("easylan.text.setting2", "Command Support"), this.width / 2 - 45, 35, 0xFF33CCFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.whitelist", "Whitelist"), this.width / 2 - 45, 60, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.ban", "Ban"), this.width / 2 - 45, 85, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.op", "Operator"), this.width / 2 - 45, 110, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.save", "Save"), this.width / 2 - 45, 135, 0xFFFFFFFF);

        matrixStack.drawString(fontRenderer, text("easylan.text.setting3", "Other (Beta)"), this.width / 2 + 75, 35, 0xFF33CCFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.httpApi", "HTTP API Info"), this.width / 2 + 75, 60, 0xFFFFFFFF);
        matrixStack.drawString(fontRenderer, text("easylan.text.lanInfo", "LAN Output"), this.width / 2 + 75, 85, 0xFFFFFFFF);

        matrixStack.drawString(fontRenderer, text("easylan.text.motd", "MOTD (Server Info)"), this.width / 2 - 165, 190, 0xFFFFFFFF);
    }

    private void saveConfig() {
        spawnAnimals = mobSpawningEnabled;
        spawnNPCs = mobSpawningEnabled;
        motd = motdText;
        ConfigUtil.save();
    }

    private static Component text(String key, String fallback) {
        String translated = I18n.get(key);
        if (translated == null || translated.isBlank() || translated.equals(key)) {
            translated = fallback;
        }
        return Component.literal(translated);
    }
}
