package org.xiaoxian.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;

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
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.addVertex(x + width, y, 0.0F).setColor(red, green, blue, alpha);
        bufferBuilder.addVertex(x, y, 0.0F).setColor(red, green, blue, alpha);
        bufferBuilder.addVertex(x, y + height, 0.0F).setColor(red, green, blue, alpha);
        bufferBuilder.addVertex(x + width, y + height, 0.0F).setColor(red, green, blue, alpha);

        BufferUploader.drawWithShader(Objects.requireNonNull(bufferBuilder.build()));

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static void drawLine(int startX, int endX, int y, int color) {
        RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, ((color >> 24) & 0xFF) / 255.0F);
        RenderSystem.lineWidth(2f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.addVertex(startX, y, 0.0F).setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, ((color >> 24) & 0xFF) / 255.0F);
        bufferBuilder.addVertex(endX, y, 0.0F).setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, ((color >> 24) & 0xFF) / 255.0F);

        BufferUploader.drawWithShader(Objects.requireNonNull(bufferBuilder.build()));
    }
}