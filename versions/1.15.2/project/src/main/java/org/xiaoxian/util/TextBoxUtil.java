package org.xiaoxian.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;

import java.awt.Color;
import java.lang.reflect.Field;

import static org.xiaoxian.util.DrawUtil.drawLine;

public class TextBoxUtil extends EditBox {
    private static final String[] DISPLAY_POS_FIELDS = { "displayPos", "field_146225_q" };

    private final Field lineScrollOffsetField;
    private long lastUpdateTick = System.currentTimeMillis();

    public TextBoxUtil(Font font, int x, int y, int width, int height, String msg) {
        super(font, x, y, width, height, msg);
        lineScrollOffsetField = resolveLineScrollOffsetField();
        if (lineScrollOffsetField != null) {
            lineScrollOffsetField.setAccessible(true);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            fill(x, y, x + width + 4, y + height, new Color(128, 128, 128, 30).getRGB());
            GlStateManager.lineWidth(2f);
            drawLine(x, x + width + 3, y + height - 1, new Color(135, 206, 250).getRGB());
            GlStateManager.lineWidth(1f);
            int textColor = this.isFocused() ? 14737632 : 7368816;

            int lineScrollOffset = 0;
            if (lineScrollOffsetField != null) {
                try {
                    lineScrollOffset = (int) lineScrollOffsetField.get(this);
                } catch (IllegalAccessException ignored) {
                }
            }
            String textToDraw = getValue().substring(Math.max(0, lineScrollOffset));

            if (this.isFocused()) {
                long currentTick = System.currentTimeMillis();
                if (currentTick - lastUpdateTick > 10) {
                    textToDraw += "_";
                    lastUpdateTick = currentTick;
                }
            }

            Minecraft.getInstance().font.drawShadow(textToDraw, x + 4, y + (float) (height - 8) / 2, textColor);
        }
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
