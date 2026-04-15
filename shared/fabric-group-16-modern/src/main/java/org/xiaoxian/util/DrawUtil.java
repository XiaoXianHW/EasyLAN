package org.xiaoxian.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class DrawUtil {
    public static void drawRect(int x, int y, int width, int height, Color color) {
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;
        float alpha = color.getAlpha() / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color4f(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferBuilder.vertex((double) x + width, y, 0.0D).endVertex();
        bufferBuilder.vertex(x, y, 0.0D).endVertex();
        bufferBuilder.vertex(x, (double) y + height, 0.0D).endVertex();
        bufferBuilder.vertex((double) x + width, (double) y + height, 0.0D).endVertex();
        tessellator.end();

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
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
