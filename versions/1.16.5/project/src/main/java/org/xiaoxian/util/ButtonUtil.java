package org.xiaoxian.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.awt.Color;

public class ButtonUtil extends Button {

    public ButtonUtil(int x, int y, int width, int height, String buttonText) {
        super(x, y, width, height, ITextComponent.nullToEmpty(buttonText), button -> {});
    }

    @Override
    public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            RenderSystem.enableBlend();
            Color color;
            if (isHovered()) {
                color = new Color(128, 128, 128, 128); // 灰色，半透明
            } else {
                color = new Color(64, 64, 64, 128); // 深灰色，半透明
            }
            fill(matrixStack, this.x, this.y, this.x + this.width, this.y + this.height, color.getRGB());
            RenderSystem.disableBlend();

            drawCenteredString(matrixStack, Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
        }
    }
}
