package org.xiaoxian.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class DrawUtil {
    public static void drawRect(int x, int y, int width, int height, Color color) {
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;
        float alpha = color.getAlpha() / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((double)x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x, (double)y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex((double)x + width, (double)y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        tesselator.end();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static void drawLine(int startX, int endX, int y, int color) {
        RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, ((color >> 24) & 0xFF) / 255.0F);
        RenderSystem.lineWidth(2f);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(startX, y, 0.0D).color(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, ((color >> 24) & 0xFF) / 255.0F).endVertex();
        builder.vertex(endX, y, 0.0D).color(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, ((color >> 24) & 0xFF) / 255.0F).endVertex();
        Tesselator.getInstance().end();
    }
}