package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;

import static org.xiaoxian.EasyLan.devMode;

public class TextBoxUtil extends GuiTextField {

    String fieldName = devMode ? "lineScrollOffset" : "field_146225_q";
    private Field lineScrollOffsetField;
    private long lastUpdateTick = 20;

    public TextBoxUtil(int componentId, FontRenderer fontRendererInstance, int x, int y, int width, int height) {
        super(componentId, fontRendererInstance, x, y, width, height);

        try {
            lineScrollOffsetField = GuiTextField.class.getDeclaredField(fieldName);
            lineScrollOffsetField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawTextBox() {
        if (this.getVisible()) {
            drawRect(xPosition, yPosition, xPosition + width + 4, yPosition + height, new Color(128, 128, 128, 30).getRGB());
            GL11.glLineWidth(2f);
            drawHorizontalLine(xPosition, xPosition + width + 3, yPosition + height - 1, new Color(135,206,250).getRGB());
            GL11.glLineWidth(1f);
            int textColor = this.getEnableBackgroundDrawing() ? 14737632 : 7368816;

            int lineScrollOffset = 0;
            try {
                lineScrollOffset = (int) lineScrollOffsetField.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            String textToDraw = getText().substring(Math.max(0, lineScrollOffset));

            if (isFocused()) {
                long currentTick = System.currentTimeMillis();
                if (currentTick - lastUpdateTick > 10) {
                    textToDraw += "_";
                    lastUpdateTick = currentTick;
                }
            }
            drawString(Minecraft.getMinecraft().fontRendererObj, textToDraw, xPosition + 4, yPosition + (height - 8) / 2, textColor);
        }
    }
}
