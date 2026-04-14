package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

public class ButtonUtil extends GuiButton {
    public ButtonUtil(int x, int y, int width, int height, String buttonText) {
        super(0, x, y, width, height, buttonText);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }

        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        GlStateManager.enableBlend();
        Color color = isMouseOver() ? new Color(128, 128, 128, 128) : new Color(64, 64, 64, 128);
        drawRect(x, y, x + width, y + height, color.getRGB());
        GlStateManager.disableBlend();

        drawCenteredString(Minecraft.getInstance().fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, 0xFFFFFF);
    }
}
