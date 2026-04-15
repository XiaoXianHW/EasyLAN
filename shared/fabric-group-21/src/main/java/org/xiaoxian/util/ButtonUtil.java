package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ButtonUtil extends Button {

    public ButtonUtil(Builder builder) {
        super(builder.x, builder.y, builder.width, builder.height, Component.nullToEmpty(builder.buttonText), button -> {}, DEFAULT_NARRATION);
    }

    public static Builder builder(int x, int y, int width, int height, String buttonText) {
        return new Builder(x, y, width, height, buttonText);
    }

    @Override
    protected void renderWidget(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        int backgroundColor = this.isHovered ? 0x80808080 : 0x40404080;
        matrixStack.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, backgroundColor);
        matrixStack.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, this.active ? 0xFFFFFF : 0xA0A0A0);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public static class Builder {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final String buttonText;

        private Builder(int x, int y, int width, int height, String buttonText) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.buttonText = buttonText;
        }
    }
}
