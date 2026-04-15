package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.Color;

public class ButtonUtil extends Button {
    public static class Builder {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final String text;

        private Builder(int x, int y, int width, int height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }
    }

    public ButtonUtil(Builder builder) {
        super(builder.x, builder.y, builder.width, builder.height, Component.nullToEmpty(builder.text), button -> {
        }, DEFAULT_NARRATION);
    }

    public static Builder builder(int x, int y, int width, int height, String buttonText) {
        return new Builder(x, y, width, height, buttonText);
    }

    @Override
    public void onPress() {
        handleClick();
    }

    protected void handleClick() {
    }

    @Override
    protected void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        Color color = this.isHovered ? new Color(128, 128, 128, 128) : new Color(64, 64, 64, 128);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color.getRGB());
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor());
    }
}
