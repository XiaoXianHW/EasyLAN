package org.xiaoxian.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
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
    private EditBox motdTextBox;
    private String motdText = motd;
    private boolean mobSpawningEnabled = spawnAnimals && spawnNPCs;
    private final Font font = Minecraft.getInstance().font;
    private final Screen parentScreen;

    public GuiEasyLanMain(Screen parentScreen) {
        super(new TranslatableComponent("easylan.setting"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        this.buttons.clear();
        this.children.clear();
        mobSpawningEnabled = spawnAnimals && spawnNPCs;

        addButton(new ButtonUtil(this.width / 2 + 70, this.height - 25, 100, 20, I18n.get("easylan.back")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                Minecraft.getInstance().setScreen(parentScreen);
            }
        });
        addButton(new ButtonUtil(this.width / 2 - 50, this.height - 25, 100, 20, I18n.get("easylan.load")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                ConfigUtil.load();
                minecraft.setScreen(new GuiEasyLanMain(parentScreen));
            }
        });
        addButton(new ButtonUtil(this.width / 2 - 170, this.height - 25, 100, 20, I18n.get("easylan.save")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                saveConfig();
            }
        });

        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 55, allowPVP, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                allowPVP = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 80, onlineMode, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                onlineMode = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 118, mobSpawningEnabled, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                mobSpawningEnabled = this.isChecked();
                spawnAnimals = mobSpawningEnabled;
                spawnNPCs = mobSpawningEnabled;
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 144, allowFlight, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                allowFlight = this.isChecked();
            }
        });

        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 55, whiteList, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                whiteList = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 80, BanCommands, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                BanCommands = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 105, OpCommands, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                OpCommands = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 130, SaveCommands, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                SaveCommands = this.isChecked();
            }
        });

        addButton(new CheckBoxButtonUtil(this.width / 2 + 145, 55, HttpAPI, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                HttpAPI = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 145, 80, LanOutput, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                LanOutput = this.isChecked();
            }
        });

        motdTextBox = new TextBoxUtil(font, this.width / 2 - 70, 185, 230, 20, "");
        motdTextBox.setMaxLength(100);
        motdTextBox.setValue(motdText);
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, font, I18n.get("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        drawString(matrixStack, font, I18n.get("easylan.text.setting1"), this.width / 2 - 165, 35, 0x33CCFF);
        drawString(matrixStack, font, I18n.get("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.spawnNPCs"), this.width / 2 - 165, 125, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.allowFlight"), this.width / 2 - 165, 150, 0xFFFFFF);

        drawString(matrixStack, font, I18n.get("easylan.text.setting2"), this.width / 2 - 45, 35, 0x33CCFF);
        drawString(matrixStack, font, I18n.get("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFF);

        drawString(matrixStack, font, I18n.get("easylan.text.setting3"), this.width / 2 + 75, 35, 0x33CCFF);
        drawString(matrixStack, font, I18n.get("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFF);
        drawString(matrixStack, font, I18n.get("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFF);

        drawString(matrixStack, font, I18n.get("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFF);
        motdTextBox.render(matrixStack, mouseX, mouseY, partialTicks);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        motdTextBox.keyPressed(keyCode, scanCode, modifiers);
        motdText = motdTextBox.getValue();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        motdTextBox.charTyped(typedChar, keyCode);
        motdText = motdTextBox.getValue();
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        motdTextBox.mouseClicked(mouseX, mouseY, mouseButton);
        motdText = motdTextBox.getValue();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void saveConfig() {
        spawnAnimals = mobSpawningEnabled;
        spawnNPCs = mobSpawningEnabled;
        motd = motdTextBox.getValue();
        ConfigUtil.save();
    }
}
