package org.xiaoxian.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.xiaoxian.util.ButtonUtil;
import org.xiaoxian.util.CheckBoxButtonUtil;
import org.xiaoxian.util.ConfigUtil;
import org.xiaoxian.util.TextBoxUtil;

import javax.annotation.Nonnull;
import java.awt.*;

import static org.xiaoxian.EasyLAN.*;

public class GuiEasyLanMain extends Screen {
    private EditBox MotdTextBox;
    private String MotdText = motd;
    Font fontRenderer = Minecraft.getInstance().font;
    private final Screen parentScreen;

    public GuiEasyLanMain(Screen parentScreen) {
        super(Component.translatable("easylan.setting"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        renderables.clear();

        // 设置
        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 + 70, this.height - 25, 100, 20, I18n.get("easylan.back"))) {
            public void onClick(double mouseX, double mouseY) {
                Minecraft.getInstance().setScreen(parentScreen);
            }
        });
        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 - 50, this.height - 25, 100, 20, I18n.get("easylan.load"))) {
            public void onClick(double mouseX, double mouseY) {
                ConfigUtil.load();
                MotdText = motd;
                init();
            }
        });
        addRenderableWidget(new ButtonUtil(ButtonUtil.builder(this.width / 2 - 170, this.height - 25, 100, 20, I18n.get("easylan.save"))) {
            public void onClick(double mouseX, double mouseY) {
                SaveConfig();
            }
        });

        // 基础设置
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 55, allowPVP, 20, 20) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                allowPVP = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 80, onlineMode, 20, 20) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                onlineMode = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95, 112, spawnAnimals, 20, 20) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                spawnAnimals = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width / 2 - 95,144 , allowFlight ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                allowFlight = this.isChecked();
            }
        });

        // 命令支持
        addRenderableWidget(new CheckBoxButtonUtil(this.width /2 +25 ,55 ,whiteList ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                whiteList = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width /2 +25 ,80 ,BanCommands ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                BanCommands = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width /2 +25 ,105 ,OpCommands ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                OpCommands = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width /2 +25 ,130 ,SaveCommands ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                SaveCommands = this.isChecked();
            }
        });

        // 其他设置
        addRenderableWidget(new CheckBoxButtonUtil(this.width /2 +145 ,55 ,HttpAPI ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                HttpAPI = this.isChecked();
            }
        });
        addRenderableWidget(new CheckBoxButtonUtil(this.width /2 +145 ,80 ,LanOutput ,20 ,20 ) {
            public void onClick(double mouseX, double mouseY) {
                this.toggleChecked();
                LanOutput = this.isChecked();
            }
        });

        // Motd
        MotdTextBox = new TextBoxUtil(fontRenderer,this.width / 2 - 70, 185, 230, 20,"");
        MotdTextBox.setMaxLength(100);
        MotdTextBox.setValue(MotdText);
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        // 标题
        drawCenteredString(matrixStack, fontRenderer, I18n.get("easylan.setting"), this.width / 2, 15, Color.WHITE.getRGB());

        // 基础设置
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.setting1"), this.width / 2 - 165, 35, 0x33CCFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.pvp"), this.width / 2 - 165, 60, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.onlineMode"), this.width / 2 - 165, 85, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.spawnAnimals"), this.width / 2 - 165, 110, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.spawnNPCs"), this.width / 2 - 165, 125, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.allowFlight"), this.width / 2 - 165, 150, 0xFFFFFF);

        // 指令支持
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.setting2"), this.width / 2 - 45, 35, 0x33CCFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.whitelist"), this.width / 2 - 45, 60, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.ban"), this.width / 2 - 45, 85, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.op"), this.width / 2 - 45, 110, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.save"), this.width / 2 - 45, 135, 0xFFFFFF);

        // 其他设置
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.setting3"), this.width / 2 + 75, 35, 0x33CCFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.httpApi"), this.width / 2 + 75, 60, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.lanInfo"), this.width / 2 + 75, 85, 0xFFFFFF);

        // MOTD
        drawString(matrixStack, fontRenderer, I18n.get("easylan.text.motd"), this.width / 2 - 165, 190, 0xFFFFFF);
        MotdTextBox.render(matrixStack, mouseX, mouseY, partialTicks);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        MotdTextBox.keyPressed(keyCode, scanCode, modifiers);
        MotdText = MotdTextBox.getValue();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        MotdTextBox.charTyped(typedChar, keyCode);
        MotdText = MotdTextBox.getValue();
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        MotdTextBox.mouseClicked(mouseX, mouseY, mouseButton);
        MotdText = MotdTextBox.getValue();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void SaveConfig() {
        ConfigUtil.set("pvp", String.valueOf(allowPVP));
        ConfigUtil.set("online-mode", String.valueOf(onlineMode));
        ConfigUtil.set("spawn-Animals", String.valueOf(spawnAnimals));
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
