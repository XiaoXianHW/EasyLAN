package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;

public class TextBoxUtil extends GuiTextField {
    private static final String[] LINE_SCROLL_OFFSET_FIELD_NAMES = { "lineScrollOffset", "field_146225_q" };
    private Field lineScrollOffsetField;
    private long lastUpdateTick = 20;

    public TextBoxUtil(int componentId, FontRenderer fontRendererInstance, int x, int y, int width, int height) {
        super(componentId, fontRendererInstance, x, y, width, height);
        lineScrollOffsetField = resolveLineScrollOffsetField();
    }

    @Override
    public void drawTextBox() {
        if (this.getVisible()) {
            drawRect(xPosition, yPosition, xPosition + width + 4, yPosition + height, new Color(128, 128, 128, 30).getRGB());
            GL11.glLineWidth(2f);
            drawHorizontalLine(xPosition, xPosition + width + 3, yPosition + height - 1, new Color(135,206,250).getRGB());
            GL11.glLineWidth(1f);
            int textColor = this.getEnableBackgroundDrawing() ? 14737632 : 7368816;

            String text = getText();
            int lineScrollOffset = Math.max(0, getLineScrollOffset());
            if (lineScrollOffset > text.length()) {
                lineScrollOffset = text.length();
            }
            String textToDraw = text.substring(lineScrollOffset);

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

    private Field resolveLineScrollOffsetField() {
        for (String fieldName : LINE_SCROLL_OFFSET_FIELD_NAMES) {
            try {
                Field field = GuiTextField.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        System.out.println("[EasyLan | TextBoxUtil] Error: missing line scroll offset field");
        return null;
    }

    private int getLineScrollOffset() {
        if (lineScrollOffsetField == null) {
            return 0;
        }

        try {
            return (int) lineScrollOffsetField.get(this);
        } catch (IllegalAccessException e) {
            System.out.println("[EasyLan | drawTextBox] Error: " + e.getMessage());
            return 0;
        }
    }
}
