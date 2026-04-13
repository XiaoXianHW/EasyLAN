package org.xiaoxian.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.xiaoxian.easylan.core.validation.ValidationRules;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.CheckBoxButtonUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import java.awt.*;
import java.io.IOException;

import static org.xiaoxian.EasyLAN.*;

public class GuiEasyLanMain extends GuiScreen {
    private GuiTextField motdTextBox;
    private String motdText = motd;

    private final GuiScreen parentScreen;

    public GuiEasyLanMain(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        motdText = motd;

        buttonList.add(new ButtonUtil(0, this.width / 2 + 70, this.height - 25, 100, 20, I18n.format("easylan.back")));
        buttonList.add(new ButtonUtil(1, this.width / 2 - 50, this.height - 25, 100, 20, I18n.format("easylan.load")));
        buttonList.add(new ButtonUtil(2, this.width / 2 - 170, this.height - 25, 100, 20, I18n.format("easylan.save")));

        buttonList.add(new CheckBoxButtonUtil(10, this.width / 2 - 95, 55, allowPVP, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(11, this.width / 2 - 95, 80, onlineMode, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(12, this.width / 2 - 95, 105, spawnAnimals, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(13, this.width / 2 - 95, 130, spawnNPCs, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(14, this.width / 2 - 95, 155, allowFlight, 20, 20));

        buttonList.add(new CheckBoxButtonUtil(20, this.width / 2 + 25, 55, whiteList, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(21, this.width / 2 + 25, 80, BanCommand, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(22, this.width / 2 + 25, 105, OpCommand, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(23, this.width / 2 + 25, 130, SaveCommand, 20, 20));

        buttonList.add(new CheckBoxButtonUtil(30, this.width / 2 + 145, 55, HttpAPI, 20, 20));
        buttonList.add(new CheckBoxButtonUtil(31, this.width / 2 + 145, 80, LanOutput, 20, 20));

        motdTextBox = new TextBoxUtil(100, fontRenderer, this.width / 2 - 70, 185, 230, 20);
        motdTextBox.setMaxStringLength(100);
        motdTextBox.setText(motdText);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        drawString(fontRenderer, I18n.format("easylan.text.setting1"), this.width / 2 - 165, 35, 0x33CCFF);
        drawString(fontRenderer, I18n.format("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.spawnNPCs"), this.width / 2 - 165, 135, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.allowFlight"), this.width / 2 - 165, 160, 0xFFFFFF);

        drawString(fontRenderer, I18n.format("easylan.text.setting2"), this.width / 2 - 45, 35, 0x33CCFF);
        drawString(fontRenderer, I18n.format("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFF);

        drawString(fontRenderer, I18n.format("easylan.text.setting3"), this.width / 2 + 75, 35, 0x33CCFF);
        drawString(fontRenderer, I18n.format("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFF);
        drawString(fontRenderer, I18n.format("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFF);

        drawString(fontRenderer, I18n.format("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFF);
        motdTextBox.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        if (button.id == 1) {
            ConfigUtil.load();
            motdText = motd;
            updateGuiConfig();
            return;
        }

        if (button.id == 2) {
            applyGuiConfig();
            ConfigUtil.save();
            return;
        }

        if (button instanceof CheckBoxButtonUtil) {
            ((CheckBoxButtonUtil) button).toggleChecked();
        }

        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        motdTextBox.textboxKeyTyped(typedChar, keyCode);
        motdText = motdTextBox.getText();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        motdTextBox.mouseClicked(mouseX, mouseY, mouseButton);
        motdText = motdTextBox.getText();
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void applyGuiConfig() {
        allowPVP = getCheckBoxState(10);
        onlineMode = getCheckBoxState(11);
        spawnAnimals = getCheckBoxState(12);
        spawnNPCs = getCheckBoxState(13);
        allowFlight = getCheckBoxState(14);
        whiteList = getCheckBoxState(20);
        BanCommand = getCheckBoxState(21);
        OpCommand = getCheckBoxState(22);
        SaveCommand = getCheckBoxState(23);
        HttpAPI = getCheckBoxState(30);
        LanOutput = getCheckBoxState(31);

        motd = motdTextBox.getText();
        if (!ValidationRules.isValidMotdLength(motd)) {
            motd = motd.substring(0, 100);
            motdTextBox.setText(motd);
        }
        motdText = motd;
    }

    private boolean getCheckBoxState(int id) {
        for (GuiButton button : buttonList) {
            if (button.id == id && button instanceof CheckBoxButtonUtil) {
                return ((CheckBoxButtonUtil) button).isChecked();
            }
        }
        return false;
    }

    private void updateGuiConfig() {
        motdTextBox.setText(motdText);

        setCheckBoxState(10, allowPVP);
        setCheckBoxState(11, onlineMode);
        setCheckBoxState(12, spawnAnimals);
        setCheckBoxState(13, spawnNPCs);
        setCheckBoxState(14, allowFlight);
        setCheckBoxState(20, whiteList);
        setCheckBoxState(21, BanCommand);
        setCheckBoxState(22, OpCommand);
        setCheckBoxState(23, SaveCommand);
        setCheckBoxState(30, HttpAPI);
        setCheckBoxState(31, LanOutput);
    }

    private void setCheckBoxState(int id, boolean checked) {
        for (GuiButton button : buttonList) {
            if (button.id == id && button instanceof CheckBoxButtonUtil) {
                ((CheckBoxButtonUtil) button).setChecked(checked);
                return;
            }
        }
    }
}
