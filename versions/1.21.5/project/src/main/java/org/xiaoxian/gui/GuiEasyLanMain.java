package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.CheckBoxButtonUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
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
    private EditBox MotdTextBox;
    private String MotdText = motd;
    private boolean mobSpawningEnabled = spawnAnimals && spawnNPCs;
    private final Font fontRenderer = Minecraft.getInstance().font;
    private final Screen parentScreen;

    public GuiEasyLanMain(Screen parentScreen) {
        super(Component.translatable("easylan.setting"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        renderables.clear();
        mobSpawningEnabled = spawnAnimals && spawnNPCs;

        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 + 70, this.height - 25, 100, 20, I18n.get("easylan.back"))) {
            @Override
            protected void handleClick() {
                Minecraft.getInstance().setScreen(parentScreen);
            }
        });
        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 - 50, this.height - 25, 100, 20, I18n.get("easylan.load"))) {
            @Override
            protected void handleClick() {
                ConfigUtil.load();
                MotdText = motd;
                minecraft.setScreen(new GuiEasyLanMain(parentScreen));
            }
        });
        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 - 170, this.height - 25, 100, 20, I18n.get("easylan.save"))) {
            @Override
            protected void handleClick() {
                SaveConfig();
            }
        });

        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 55, allowPVP, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                allowPVP = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 80, onlineMode, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                onlineMode = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 118, mobSpawningEnabled, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                mobSpawningEnabled = this.isChecked();
                spawnAnimals = mobSpawningEnabled;
                spawnNPCs = mobSpawningEnabled;
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 144, allowFlight, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                allowFlight = this.isChecked();
            }
        });

        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 55, whiteList, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                whiteList = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 80, BanCommands, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                BanCommands = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 105, OpCommands, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                OpCommands = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 25, 130, SaveCommands, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                SaveCommands = this.isChecked();
            }
        });

        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 145, 55, HttpAPI, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                HttpAPI = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 + 145, 80, LanOutput, 20, 20) {
            @Override
            protected void handleClick() {
                this.toggleChecked();
                LanOutput = this.isChecked();
            }
        });

        MotdTextBox = new TextBoxUtil(fontRenderer, this.width / 2 - 70, 185, 230, 20, "");
        MotdTextBox.setMaxLength(100);
        MotdTextBox.setValue(MotdText);
        MotdTextBox.setResponder(value -> MotdText = value);
        addRenderableWidget(MotdTextBox);
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(fontRenderer, I18n.get("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.setting1"), this.width / 2 - 165, 35, 0xFF33CCFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.spawnNPCs"), this.width / 2 - 165, 125, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.allowFlight"), this.width / 2 - 165, 150, 0xFFFFFFFF);

        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.setting2"), this.width / 2 - 45, 35, 0xFF33CCFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFFFF);

        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.setting3"), this.width / 2 + 75, 35, 0xFF33CCFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFFFF);
        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFFFF);

        guiGraphics.drawString(fontRenderer, I18n.get("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFFFF);
    }

    public void SaveConfig() {
        spawnAnimals = mobSpawningEnabled;
        spawnNPCs = mobSpawningEnabled;
        ConfigUtil.set("pvp", String.valueOf(allowPVP));
        ConfigUtil.set("online-mode", String.valueOf(onlineMode));
        ConfigUtil.set("spawn-Animals", String.valueOf(mobSpawningEnabled));
        ConfigUtil.set("spawn-NPCs", String.valueOf(mobSpawningEnabled));
        ConfigUtil.set("allow-Flight", String.valueOf(allowFlight));
        ConfigUtil.set("whiteList", String.valueOf(whiteList));
        ConfigUtil.set("BanCommands", String.valueOf(BanCommands));
        ConfigUtil.set("OpCommands", String.valueOf(OpCommands));
        ConfigUtil.set("SaveCommands", String.valueOf(SaveCommands));
        ConfigUtil.set("Http-Api", String.valueOf(HttpAPI));
        ConfigUtil.set("Lan-output", String.valueOf(LanOutput));
        motd = MotdTextBox.getValue();
        ConfigUtil.set("Motd", motd);
        ConfigUtil.save();
    }
}
