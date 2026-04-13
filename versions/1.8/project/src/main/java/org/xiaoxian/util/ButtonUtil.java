package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;

public class ButtonUtil extends GuiButton {

    public ButtonUtil(int buttonId, int x, int y, int width, int height, String buttonText) {
        super(buttonId, x, y, buttonText);
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int i = this.getHoverState(this.hovered);

            FontRenderer fontrenderer = mc.fontRendererObj;

            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);

            Color color;
            if (i == 2) {
                color = new Color(128, 128, 128, 128); // 灰色，半透明
            } else {
                color = new Color(64, 64, 64, 128); // 深灰色，半透明
            }
            DrawUtil.drawRect(this.xPosition, this.yPosition, this.width, this.height, color);

            GL11.glDisable(GL11.GL_BLEND);

            int j = 14737632;
            if (packedFGColour != 0) {
                j = packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }
            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }
}
