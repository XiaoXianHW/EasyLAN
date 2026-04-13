package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;

public class TextBoxUtil extends GuiTextField {
    private static final String[] LINE_SCROLL_FIELDS = { "lineScrollOffset", "field_146225_q" };

    private final Field lineScrollOffsetField;
    private long lastUpdateTick = System.currentTimeMillis();

    public TextBoxUtil(int componentId, FontRenderer fontRendererInstance, int x, int y, int width, int height) {
        super(componentId, fontRendererInstance, x, y, width, height);
        lineScrollOffsetField = resolveLineScrollOffsetField();
        if (lineScrollOffsetField != null) {
            lineScrollOffsetField.setAccessible(true);
        }
    }

    @Override
    public void drawTextBox() {
        if (this.getVisible()) {
            drawRect(x, y, x + width + 4, y + height, new Color(128, 128, 128, 30).getRGB());
            GL11.glLineWidth(2f);
            drawHorizontalLine(x, x + width + 3, y + height - 1, new Color(135, 206, 250).getRGB());
            GL11.glLineWidth(1f);
            int textColor = this.getEnableBackgroundDrawing() ? 14737632 : 7368816;

            String textToDraw = getText().substring(Math.max(0, getLineScrollOffset()));
            if (isFocused()) {
                long currentTick = System.currentTimeMillis();
                if (currentTick - lastUpdateTick > 10) {
                    textToDraw += "_";
                    lastUpdateTick = currentTick;
                }
            }

            drawString(Minecraft.getMinecraft().fontRenderer, textToDraw, x + 4, y + (height - 8) / 2, textColor);
        }
    }

    private Field resolveLineScrollOffsetField() {
        for (String fieldName : LINE_SCROLL_FIELDS) {
            try {
                return GuiTextField.class.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private int getLineScrollOffset() {
        if (lineScrollOffsetField == null) {
            return 0;
        }

        try {
            return (int) lineScrollOffsetField.get(this);
        } catch (IllegalAccessException ignored) {
            return 0;
        }
    }
}
