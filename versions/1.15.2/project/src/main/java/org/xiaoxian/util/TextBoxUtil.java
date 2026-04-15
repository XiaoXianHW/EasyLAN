package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;

import java.awt.Color;
import java.lang.reflect.Field;

public class TextBoxUtil extends EditBox {
    private static final String[] DISPLAY_POS_FIELDS = { "displayPos", "field_146225_q" };

    private final Field lineScrollOffsetField;

    public TextBoxUtil(Font font, int x, int y, int width, int height, String msg) {
        super(font, x, y, width, height, msg);
        lineScrollOffsetField = resolveLineScrollOffsetField();
        if (lineScrollOffsetField != null) {
            lineScrollOffsetField.setAccessible(true);
        }
        setBordered(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        fill(x, y, x + width + 4, y + height, new Color(48, 48, 48, 170).getRGB());
        fill(x, y + height - 1, x + width + 4, y + height, new Color(135, 206, 250, 230).getRGB());
        int textColor = this.isFocused() ? 14737632 : 7368816;

        int lineScrollOffset = 0;
        if (lineScrollOffsetField != null) {
            try {
                lineScrollOffset = (int) lineScrollOffsetField.get(this);
            } catch (IllegalAccessException ignored) {
            }
        }

        String textToDraw = getValue().substring(Math.max(0, lineScrollOffset));
        if (this.isFocused() && (System.currentTimeMillis() / 300L) % 2L == 0L) {
            textToDraw += "_";
        }

        Minecraft.getInstance().font.drawShadow(textToDraw, x + 4, y + (float) (height - 8) / 2, textColor);
    }

    private Field resolveLineScrollOffsetField() {
        for (String candidate : DISPLAY_POS_FIELDS) {
            try {
                return EditBox.class.getDeclaredField(candidate);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
