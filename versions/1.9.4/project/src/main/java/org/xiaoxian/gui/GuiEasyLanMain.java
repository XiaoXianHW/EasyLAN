package org.xiaoxian.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.CheckBoxButtonUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.xiaoxian.EasyLAN.*;

public class GuiEasyLanMain extends GuiScreen {
    private GuiTextField MotdTextBox;
    private String MotdText = motd;

    private final GuiScreen parentScreen;
    private final List<GuiButton> buttonList = new ArrayList<>();

    public GuiEasyLanMain(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        // 设置
        buttonList.add(new ButtonUtil(0, this.width / 2 + 70, this.height - 25, 100, 20, I18n.format("easylan.back")));
        buttonList.add(new ButtonUtil(1, this.width / 2 - 50, this.height - 25, 100, 20, I18n.format("easylan.load")));
        buttonList.add(new ButtonUtil(2, this.width / 2 - 170, this.height - 25, 100, 20, I18n.format("easylan.save")));

        // 基础设置
        buttonList.add(new CheckBoxButtonUtil(10, this.width / 2 - 95, 55, allowPVP, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(11, this.width / 2 - 95, 80, onlineMode, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(12, this.width / 2 - 95, 105, spawnAnimals, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(13, this.width / 2 - 95, 130, spawnNPCs, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(14, this.width / 2 - 95, 155, allowFlight, 20, 20));

        // 命令支持
        buttonList.add(new CheckBoxButtonUtil(20, this.width / 2 + 25, 55, whiteList, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(21, this.width / 2 + 25, 80, BanCommand, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(22, this.width / 2 + 25, 105, OpCommand, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(23, this.width / 2 + 25, 130, SaveCommand, 20, 20));

        // 其他设置
        buttonList.add(new CheckBoxButtonUtil(30, this.width / 2 + 145, 55, HttpAPI, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(31, this.width / 2 + 145, 80, LanOutput, 20, 20));

        // Motd
        MotdTextBox = new TextBoxUtil(100, fontRendererObj, this.width / 2 - 70, 185, 230, 20);
        MotdTextBox.setMaxStringLength(100);
        MotdTextBox.setText(MotdText);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        // 标题
        drawCenteredString(fontRendererObj, I18n.format("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        // 基础设置
        drawString(fontRendererObj, I18n.format("easylan.text.setting1"), this.width / 2 - 165, 35, 0x33CCFF);
        drawString(fontRendererObj, I18n.format("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.spawnNPCs"), this.width / 2 - 165, 135, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.allowFlight"), this.width / 2 - 165, 160, 0xFFFFFF);

        // 指令支持
        drawString(fontRendererObj, I18n.format("easylan.text.setting2"), this.width / 2 - 45, 35, 0x33CCFF);
        drawString(fontRendererObj, I18n.format("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFF);

        // 其他设置
        drawString(fontRendererObj, I18n.format("easylan.text.setting3"), this.width / 2 + 75, 35, 0x33CCFF);
        drawString(fontRendererObj, I18n.format("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFF);

        // MOTD
        drawString(fontRendererObj, I18n.format("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFF);
        MotdTextBox.drawTextBox();

        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(parentScreen);
        } else if (button.id == 1) {
            ConfigUtil.load();
            MotdText = motd;
            updateGuiConfig();
        } else if (button.id == 2) {
            for (GuiButton CheckButton : buttonList) {
                if (CheckButton instanceof CheckBoxButtonUtil) {
                    CheckBoxButtonUtil checkBox = (CheckBoxButtonUtil) CheckButton;
                    switch (checkBox.id) {
                        case 10:
                            allowPVP = checkBox.isChecked();
                            ConfigUtil.set("pvp", String.valueOf(allowPVP));
                            break;
                        case 11:
                            onlineMode = checkBox.isChecked();
                            ConfigUtil.set("online-mode", String.valueOf(onlineMode));
                            break;
                        case 12:
                            spawnAnimals = checkBox.isChecked();
                            ConfigUtil.set("spawn-Animals", String.valueOf(spawnAnimals));
                            break;
                        case 13:
                            spawnNPCs = checkBox.isChecked();
                            ConfigUtil.set("spawn-NPCs", String.valueOf(spawnNPCs));
                            break;
                        case 14:
                            allowFlight = checkBox.isChecked();
                            ConfigUtil.set("allow-Flight", String.valueOf(allowFlight));
                            break;
                        case 20:
                            whiteList = checkBox.isChecked();
                            ConfigUtil.set("whiteList", String.valueOf(whiteList));
                            break;
                        case 21:
                            BanCommand = checkBox.isChecked();
                            ConfigUtil.set("BanCommand", String.valueOf(BanCommand));
                            break;
                        case 22:
                            OpCommand = checkBox.isChecked();
                            ConfigUtil.set("OpCommand", String.valueOf(OpCommand));
                            break;
                        case 23:
                            SaveCommand = checkBox.isChecked();
                            ConfigUtil.set("SaveCommand", String.valueOf(SaveCommand));
                            break;
                        case 30:
                            HttpAPI = checkBox.isChecked();
                            ConfigUtil.set("Http-Api", String.valueOf(HttpAPI));
                            break;
                        case 31:
                            LanOutput = checkBox.isChecked();
                            ConfigUtil.set("Lan-output", String.valueOf(LanOutput));
                            break;
                        default:
                            break;
                    }
                }
            }
            motd = MotdTextBox.getText();
            ConfigUtil.set("Motd", motd);
            ConfigUtil.save();
        }

        if (button instanceof CheckBoxButtonUtil) {
            CheckBoxButtonUtil checkBox = (CheckBoxButtonUtil) button;
            checkBox.toggleChecked();
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException{
        MotdTextBox.textboxKeyTyped(typedChar, keyCode);
        MotdText = MotdTextBox.getText();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
        MotdTextBox.mouseClicked(mouseX, mouseY, mouseButton);
        MotdText = MotdTextBox.getText();
        for (GuiButton button : buttonList) {
            if (button.mousePressed(mc, mouseX, mouseY)) {
                actionPerformed(button);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void updateGuiConfig() {
        // 更新文本框状态
        MotdTextBox.setText(MotdText);

        // 更新复选框状态
        for (GuiButton checkButton : buttonList) {
            if (checkButton instanceof CheckBoxButtonUtil) {
                CheckBoxButtonUtil checkBox = (CheckBoxButtonUtil) checkButton;
                switch (checkBox.id) {
                    case 10:
                        checkBox.setChecked(allowPVP);
                        break;
                    case 11:
                        checkBox.setChecked(onlineMode);
                        break;
                    case 12:
                        checkBox.setChecked(spawnAnimals);
                        break;
                    case 13:
                        checkBox.setChecked(spawnNPCs);
                        break;
                    case 14:
                        checkBox.setChecked(allowFlight);
                        break;
                    case 20:
                        checkBox.setChecked(whiteList);
                        break;
                    case 21:
                        checkBox.setChecked(BanCommand);
                        break;
                    case 22:
                        checkBox.setChecked(OpCommand);
                        break;
                    case 23:
                        checkBox.setChecked(SaveCommand);
                        break;
                    case 30:
                        checkBox.setChecked(HttpAPI);
                        break;
                    case 31:
                        checkBox.setChecked(LanOutput);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
