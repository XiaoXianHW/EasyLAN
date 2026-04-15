package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.xiaoxian.util.ConfigUtil;

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
    private final Screen parentScreen;

    public GuiEasyLanMain(Screen parentScreen) {
        super(Component.translatable("easylan.setting"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        mobSpawningEnabled = spawnAnimals && spawnNPCs;

        addRenderableWidget(Button.builder(Component.literal(I18n.get("easylan.back")), button ->
                Minecraft.getInstance().setScreen(parentScreen)
        ).bounds(this.width / 2 + 70, this.height - 25, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal(I18n.get("easylan.load")), button -> {
            ConfigUtil.load();
            motdText = motd;
            Minecraft.getInstance().setScreen(new GuiEasyLanMain(parentScreen));
        }).bounds(this.width / 2 - 50, this.height - 25, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal(I18n.get("easylan.save")), button -> saveConfig())
                .bounds(this.width / 2 - 170, this.height - 25, 100, 20)
                .build());

        addRenderableWidget(createToggleButton(this.width / 2 - 95, 55, () -> allowPVP, value -> allowPVP = value));
        addRenderableWidget(createToggleButton(this.width / 2 - 95, 80, () -> onlineMode, value -> onlineMode = value));
        addRenderableWidget(createToggleButton(this.width / 2 - 95, 118, () -> mobSpawningEnabled, value -> {
            mobSpawningEnabled = value;
            spawnAnimals = value;
            spawnNPCs = value;
        }));
        addRenderableWidget(createToggleButton(this.width / 2 - 95, 144, () -> allowFlight, value -> allowFlight = value));

        addRenderableWidget(createToggleButton(this.width / 2 + 25, 55, () -> whiteList, value -> whiteList = value));
        addRenderableWidget(createToggleButton(this.width / 2 + 25, 80, () -> BanCommands, value -> BanCommands = value));
        addRenderableWidget(createToggleButton(this.width / 2 + 25, 105, () -> OpCommands, value -> OpCommands = value));
        addRenderableWidget(createToggleButton(this.width / 2 + 25, 130, () -> SaveCommands, value -> SaveCommands = value));

        addRenderableWidget(createToggleButton(this.width / 2 + 145, 55, () -> HttpAPI, value -> HttpAPI = value));
        addRenderableWidget(createToggleButton(this.width / 2 + 145, 80, () -> LanOutput, value -> LanOutput = value));

        motdTextBox = new EditBox(this.font, this.width / 2 - 70, 185, 230, 20, Component.empty());
        motdTextBox.setMaxLength(100);
        motdTextBox.setValue(motdText);
        motdTextBox.setResponder(value -> motdText = value);
        addRenderableWidget(motdTextBox);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(this.font, I18n.get("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        guiGraphics.drawString(this.font, I18n.get("easylan.text.setting1"), this.width / 2 - 165, 35, 0x33CCFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.spawnNPCs"), this.width / 2 - 165, 125, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.allowFlight"), this.width / 2 - 165, 150, 0xFFFFFF);

        guiGraphics.drawString(this.font, I18n.get("easylan.text.setting2"), this.width / 2 - 45, 35, 0x33CCFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFF);

        guiGraphics.drawString(this.font, I18n.get("easylan.text.setting3"), this.width / 2 + 75, 35, 0x33CCFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFF);
        guiGraphics.drawString(this.font, I18n.get("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFF);

        guiGraphics.drawString(this.font, I18n.get("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFF);
    }

    private Button createToggleButton(int x, int y, BoolGetter getter, BoolSetter setter) {
        return Button.builder(toggleMessage(getter.get()), button -> {
            setter.set(!getter.get());
            button.setMessage(toggleMessage(getter.get()));
        }).bounds(x, y, 60, 20).build();
    }

    private Component toggleMessage(boolean enabled) {
        return Component.literal(enabled ? "ON" : "OFF");
    }

    private void saveConfig() {
        spawnAnimals = mobSpawningEnabled;
        spawnNPCs = mobSpawningEnabled;
        motd = motdTextBox.getValue();
        ConfigUtil.save();
    }

    @FunctionalInterface
    private interface BoolGetter {
        boolean get();
    }

    @FunctionalInterface
    private interface BoolSetter {
        void set(boolean value);
    }
}
