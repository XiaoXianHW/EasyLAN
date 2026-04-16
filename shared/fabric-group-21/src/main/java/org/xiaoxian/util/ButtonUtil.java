package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ButtonUtil extends Button {
    private static final int NORMAL_BORDER_COLOR = 0x70808080;
    private static final int HOVER_BORDER_COLOR = 0x90A7D8FF;
    private static final int NORMAL_BACKGROUND_COLOR = 0x7A303030;
    private static final int HOVER_BACKGROUND_COLOR = 0x9A4A4A4A;

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
        matrixStack.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.isHovered ? HOVER_BORDER_COLOR : NORMAL_BORDER_COLOR);
        matrixStack.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, this.isHovered ? HOVER_BACKGROUND_COLOR : NORMAL_BACKGROUND_COLOR);
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
