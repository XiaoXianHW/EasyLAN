package org.xiaoxian.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.AbstractGui;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class DrawUtil {
    public static void drawRect(int x, int y, int width, int height, Color color) {
        AbstractGui.fill(x, y, x + width, y + height, color.getRGB());
    }

    public static void drawLine(int startX, int endX, int y, int color) {
        GlStateManager.color4f(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, ((color >> 24) & 0xFF) / 255.0F);
        GlStateManager.lineWidth(2f);
        GlStateManager.disableTexture();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(startX, y);
        GL11.glVertex2i(endX, y);
        GL11.glEnd();
        GlStateManager.enableTexture();
    }
}
