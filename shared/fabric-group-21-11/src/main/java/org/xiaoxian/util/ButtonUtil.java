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
        super(builder.x, builder.y, builder.width, builder.height, builder.message, button -> {}, DEFAULT_NARRATION);
    }

    public static Builder builder(int x, int y, int width, int height, String buttonText) {
        return new Builder(x, y, width, height, Component.nullToEmpty(buttonText));
    }

    public static Builder builder(int x, int y, int width, int height, Component message) {
        return new Builder(x, y, width, height, message);
    }

    @Override
    protected void renderContents(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.isHovered ? HOVER_BORDER_COLOR : NORMAL_BORDER_COLOR);
        matrixStack.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, this.isHovered ? HOVER_BACKGROUND_COLOR : NORMAL_BACKGROUND_COLOR);
        matrixStack.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, this.active ? 0xFFFFFFFF : 0xFFA0A0A0);
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
        private final Component message;

        private Builder(int x, int y, int width, int height, Component message) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.message = message;
        }
    }
}
