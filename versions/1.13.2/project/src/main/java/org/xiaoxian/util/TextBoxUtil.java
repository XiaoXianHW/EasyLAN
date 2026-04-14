package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;
import java.lang.reflect.Field;

import static org.xiaoxian.util.DrawUtil.drawLine;

public class TextBoxUtil extends GuiTextField {
    private final Field lineScrollOffsetField;
    private long lastUpdateTick = 20L;

    public TextBoxUtil(FontRenderer fontRenderer, int x, int y, int width, int height, String text) {
        super(0, fontRenderer, x, y, width, height);
        setText(text);
        lineScrollOffsetField = resolveLineScrollOffsetField();
    }

    @Override
    public void drawTextField(int mouseX, int mouseY, float partialTicks) {
        if (!getVisible()) {
            return;
        }

        drawRect(x, y, x + width + 4, y + height, new Color(128, 128, 128, 30).getRGB());
        GlStateManager.lineWidth(2f);
        drawLine(x, x + width + 3, y + height - 1, new Color(135, 206, 250).getRGB());
        GlStateManager.lineWidth(1f);

        int textColor = isFocused() ? 14737632 : 7368816;
        int lineScrollOffset = 0;
        if (lineScrollOffsetField != null) {
            try {
                lineScrollOffset = (int) lineScrollOffsetField.get(this);
            } catch (IllegalAccessException e) {
                System.out.println("[EasyLAN | drawTextBox] Error: " + e.getMessage());
            }
        }

        String textToDraw = getText().substring(Math.max(0, lineScrollOffset));
        if (isFocused()) {
            long currentTick = System.currentTimeMillis();
            if (currentTick - lastUpdateTick > 10L) {
                textToDraw += "_";
                lastUpdateTick = currentTick;
            }
        }

        Minecraft.getInstance().fontRenderer.drawStringWithShadow(textToDraw, x + 4, y + (float) (height - 8) / 2, textColor);
    }

    private Field resolveLineScrollOffsetField() {
        String[] candidates = new String[] { "lineScrollOffset", "field_146225_q" };
        for (String candidate : candidates) {
            try {
                Field field = GuiTextField.class.getDeclaredField(candidate);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
