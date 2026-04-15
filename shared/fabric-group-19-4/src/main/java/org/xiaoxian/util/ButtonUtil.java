package org.xiaoxian.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ButtonUtil extends Button {
    public ButtonUtil(int x, int y, int width, int height, String buttonText) {
        super(x, y, width, height, Component.nullToEmpty(buttonText), button -> {}, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        int backgroundColor = this.isHovered ? 0x80808080 : 0x40404080;
        fill(matrixStack, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);

        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
        drawCenteredString(matrixStack, Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
    }
}
