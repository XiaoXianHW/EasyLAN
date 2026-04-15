package org.xiaoxian.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.Color;

public class ButtonUtil extends Button {

    public ButtonUtil(Builder builder) {
        super(builder);
    }

    public static Builder builder(int x, int y, int width, int height, String buttonText) {
        return Button.builder(Component.nullToEmpty(buttonText), button -> {}).bounds(x, y, width, height);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

            RenderSystem.enableBlend();
            Color color;
            if (isHovered) {
                color = new Color(128, 128, 128, 128);
            } else {
                color = new Color(64, 64, 64, 128);
            }
            matrixStack.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color.getRGB());
            RenderSystem.disableBlend();

            matrixStack.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor());
        }
    }
}
