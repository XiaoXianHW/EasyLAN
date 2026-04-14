package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.CheckBoxButtonUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import java.awt.Color;

import static org.xiaoxian.EasyLAN.*;

public class GuiEasyLanMain extends GuiScreen {
    private GuiTextField MotdTextBox;
    private String MotdText = motd;
    private final FontRenderer localFontRenderer = Minecraft.getInstance().fontRenderer;
    private final GuiScreen parentScreen;

    public GuiEasyLanMain(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    protected void initGui() {
        buttons.clear();

        addButton(new ButtonUtil(this.width / 2 + 70, this.height - 25, 100, 20, I18n.format("easylan.back")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                mc.displayGuiScreen(parentScreen);
            }
        });
        addButton(new ButtonUtil(this.width / 2 - 50, this.height - 25, 100, 20, I18n.format("easylan.load")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                ConfigUtil.load();
                MotdText = motd;
                mc.displayGuiScreen(new GuiEasyLanMain(parentScreen));
            }
        });
        addButton(new ButtonUtil(this.width / 2 - 170, this.height - 25, 100, 20, I18n.format("easylan.save")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                saveConfig();
            }
        });

        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 55, allowPVP, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                allowPVP = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 80, onlineMode, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                onlineMode = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 105, spawnAnimals, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                spawnAnimals = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 130, spawnNPCs, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                spawnNPCs = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 155, allowFlight, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                allowFlight = isChecked();
            }
        });

        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 55, whiteList, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                whiteList = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 80, BanCommands, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                BanCommands = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 105, OpCommands, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                OpCommands = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 25, 130, SaveCommands, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                SaveCommands = isChecked();
            }
        });

        addButton(new CheckBoxButtonUtil(this.width / 2 + 145, 55, HttpAPI, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                HttpAPI = isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 + 145, 80, LanOutput, 20, 20) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                toggleChecked();
                LanOutput = isChecked();
            }
        });

        MotdTextBox = new TextBoxUtil(localFontRenderer, this.width / 2 - 70, 185, 230, 20, "");
        MotdTextBox.setMaxStringLength(100);
        MotdTextBox.setText(MotdText);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(localFontRenderer, I18n.format("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        drawString(localFontRenderer, I18n.format("easylan.text.setting1"), this.width / 2 - 165, 35, 0x33CCFF);
        drawString(localFontRenderer, I18n.format("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.spawnNPCs"), this.width / 2 - 165, 135, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.allowFlight"), this.width / 2 - 165, 160, 0xFFFFFF);

        drawString(localFontRenderer, I18n.format("easylan.text.setting2"), this.width / 2 - 45, 35, 0x33CCFF);
        drawString(localFontRenderer, I18n.format("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFF);

        drawString(localFontRenderer, I18n.format("easylan.text.setting3"), this.width / 2 + 75, 35, 0x33CCFF);
        drawString(localFontRenderer, I18n.format("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFF);
        drawString(localFontRenderer, I18n.format("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFF);

        drawString(localFontRenderer, I18n.format("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFF);
        MotdTextBox.drawTextField(mouseX, mouseY, partialTicks);

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        MotdTextBox.keyPressed(keyCode, scanCode, modifiers);
        MotdText = MotdTextBox.getText();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        MotdTextBox.charTyped(typedChar, keyCode);
        MotdText = MotdTextBox.getText();
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        MotdTextBox.mouseClicked(mouseX, mouseY, mouseButton);
        MotdText = MotdTextBox.getText();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void saveConfig() {
        ConfigUtil.set("pvp", String.valueOf(allowPVP));
        ConfigUtil.set("online-mode", String.valueOf(onlineMode));
        ConfigUtil.set("spawn-Animals", String.valueOf(spawnAnimals));
        ConfigUtil.set("spawn-NPCs", String.valueOf(spawnNPCs));
        ConfigUtil.set("allow-Flight", String.valueOf(allowFlight));
        ConfigUtil.set("whiteList", String.valueOf(whiteList));
        ConfigUtil.set("BanCommands", String.valueOf(BanCommands));
        ConfigUtil.set("OpCommands", String.valueOf(OpCommands));
        ConfigUtil.set("SaveCommands", String.valueOf(SaveCommands));
        ConfigUtil.set("Http-Api", String.valueOf(HttpAPI));
        ConfigUtil.set("Lan-output", String.valueOf(LanOutput));
        motd = MotdTextBox.getText();
        ConfigUtil.set("Motd", motd);
        ConfigUtil.save();
    }
}
