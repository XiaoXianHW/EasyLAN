package org.xiaoxian.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.CheckBoxButtonUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import java.awt.*;

import static org.xiaoxian.EasyLAN.*;

public class GuiEasyLanMain extends Screen {
    private TextFieldWidget MotdTextBox;
    private String MotdText = motd;
    FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
    private final Screen parentScreen;

    public GuiEasyLanMain(Screen parentScreen) {
        super(new TranslationTextComponent("easylan.setting"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        buttons.clear();

        // 设置
        addButton(new ButtonUtil(this.width / 2 + 70, this.height - 25, 100, 20, I18n.format("easylan.back")) {
            public void onClick(double mouseX, double mouseY) {
                Minecraft.getInstance().displayGuiScreen(parentScreen);
            }
        });
        addButton(new ButtonUtil(this.width / 2 - 50, this.height - 25, 100, 20, I18n.format("easylan.load")) {
            public void onClick(double mouseX, double mouseY) {
                ConfigUtil.load();
                MotdText = motd;
                minecraft.displayGuiScreen(new GuiEasyLanMain(parentScreen));
            }
        });
        addButton(new ButtonUtil(this.width / 2 - 170, this.height - 25, 100, 20, I18n.format("easylan.save")) {
            public void onClick(double mouseX, double mouseY) {
                SaveConfig();
            }
        });

        // 基础设置
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 55, allowPVP, 20, 20) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                allowPVP = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 80, onlineMode, 20, 20) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                onlineMode = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 105, spawnAnimals, 20, 20) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                spawnAnimals = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95, 130, spawnNPCs, 20, 20) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                spawnNPCs = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width / 2 - 95,155 , allowFlight ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                allowFlight = this.isChecked();
            }
        });

        // 命令支持
        addButton(new CheckBoxButtonUtil(this.width /2 +25 ,55 ,whiteList ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                whiteList = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width /2 +25 ,80 ,BanCommands ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                BanCommands = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width /2 +25 ,105 ,OpCommands ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                OpCommands = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width /2 +25 ,130 ,SaveCommands ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                SaveCommands = this.isChecked();
            }
        });

        // 其他设置
        addButton(new CheckBoxButtonUtil(this.width /2 +145 ,55 ,HttpAPI ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                HttpAPI = this.isChecked();
            }
        });
        addButton(new CheckBoxButtonUtil(this.width /2 +145 ,80 ,LanOutput ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                LanOutput = this.isChecked();
            }
        });

        // Motd
        MotdTextBox = new TextBoxUtil(fontRenderer,this.width / 2 - 70, 185, 230, 20,"");
        MotdTextBox.setMaxStringLength(100);
        MotdTextBox.setText(MotdText);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        // 标题
        drawCenteredString(fontRenderer, I18n.format("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        // 基础设置
        drawString(fontRenderer, I18n.format("easylan.text.setting1"), this.width / 2 - 165, 35, 0x33CCFF);
        drawString(fontRenderer, I18n.format("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.spawnNPCs"), this.width / 2 - 165, 135, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.allowFlight"), this.width / 2 - 165, 160, 0xFFFFFF);

        // 指令支持
        drawString(fontRenderer, I18n.format("easylan.text.setting2"), this.width / 2 - 45, 35, 0x33CCFF);
        drawString(fontRenderer, I18n.format("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFF);

        // 其他设置
        drawString(fontRenderer, I18n.format("easylan.text.setting3"), this.width / 2 + 75, 35, 0x33CCFF);
        drawString(fontRenderer, I18n.format("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFF);

        // MOTD
        drawString(fontRenderer, I18n.format("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFF);
        MotdTextBox.render(mouseX,mouseY,partialTicks);

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

    public void SaveConfig() {
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
