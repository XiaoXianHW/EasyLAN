package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;
import java.awt.*;

public class ButtonUtil extends GuiButton {

    public ButtonUtil(int buttonId, int x, int y, int width, int height, String buttonText) {
        super(buttonId, x, y, buttonText);
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);

            FontRenderer fontrenderer = mc.fontRenderer;
            GlStateManager.enableBlend();
            Color color;
            if (i == 2) {
                color = new Color(128, 128, 128, 128); // 灰色，半透明
            } else {
                color = new Color(64, 64, 64, 128); // 深灰色，半透明
            }
            DrawUtil.drawRect(this.x, this.y, this.width, this.height, color);
            GlStateManager.disableBlend();

            int j = 14737632;
            if (packedFGColour != 0) {
                j = packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }
            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }
}