package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import com.mojang.blaze3d.platform.GlStateManager;

import java.awt.*;
import java.lang.reflect.Field;

import static org.xiaoxian.EasyLAN.devMode;
import static org.xiaoxian.util.DrawUtil.drawLine;

public class TextBoxUtil extends TextFieldWidget {

    String fieldName = devMode ? "lineScrollOffset" : "field_146225_q";
    private Field lineScrollOffsetField;
    private long lastUpdateTick = 20;

    public TextBoxUtil(FontRenderer fontRenderer, int x, int y, int width, int height, String msg) {
        super(fontRenderer, x, y, width, height, msg);

        try {
            lineScrollOffsetField = TextFieldWidget.class.getDeclaredField(fieldName);
            lineScrollOffsetField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            fill(x, y, x + width + 4, y + height, new Color(128, 128, 128, 30).getRGB());
            GlStateManager.lineWidth(2f);
            drawLine(x, x + width + 3, y + height - 1, new Color(135,206,250).getRGB());
            GlStateManager.lineWidth(1f);
            int textColor = this.isFocused() ? 14737632 : 7368816;

            int lineScrollOffset = 0;
            try {
                lineScrollOffset = (int) lineScrollOffsetField.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            String textToDraw = getText().substring(Math.max(0, lineScrollOffset));

            if (this.isFocused()) {
                long currentTick = System.currentTimeMillis();
                if (currentTick - lastUpdateTick > 10) {
                    textToDraw += "_";
                    lastUpdateTick = currentTick;
                }
            }

            Minecraft.getInstance().fontRenderer.drawStringWithShadow(textToDraw, x + 4, y + (height - 8) / 2, textColor);
        }
    }
}