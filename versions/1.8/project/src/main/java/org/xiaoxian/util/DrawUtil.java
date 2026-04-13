package org.xiaoxian.util;

import org.lwjgl.opengl.GL11;
import java.awt.Color;

public class DrawUtil {
    public static void drawRect(int x, int y, int width, int height, Color color) {
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;
        float alpha = color.getAlpha() / 255.0F;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(red, green, blue, alpha);

        // Draw rectangle
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x + width, y, 0.0D);
        GL11.glVertex3d(x, y, 0.0D);
        GL11.glVertex3d(x, y + height, 0.0D);
        GL11.glVertex3d(x + width, y + height, 0.0D);
        GL11.glEnd();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}


