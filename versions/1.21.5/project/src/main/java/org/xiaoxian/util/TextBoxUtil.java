package org.xiaoxian.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.Color;

public class TextBoxUtil extends EditBox {
    private final Font font;

    public TextBoxUtil(Font font, int x, int y, int width, int height, String msg) {
        super(font, x, y, width, height, Component.nullToEmpty(msg));
        this.font = font;
        setBordered(false);
        setTextColor(0xFFE0E0E0);
        setTextColorUneditable(0xFF707070);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        int left = getX();
        int top = getY();
        int right = left + width + 4;
        int bottom = top + height;
        guiGraphics.fill(left, top, right, bottom, new Color(48, 48, 48, 170).getRGB());
        guiGraphics.fill(left, bottom - 1, right, bottom, new Color(135, 206, 250, 230).getRGB());

        String value = getValue();
        String visibleText = font.plainSubstrByWidth(value, getInnerWidth());
        int textColor = isFocused() ? 0xFFE0E0E0 : 0xFFB8B8B8;
        int textY = top + (height - 8) / 2;
        guiGraphics.drawString(font, visibleText, left + 4, textY, textColor);

        if (isFocused()) {
            int cursorX = left + 4 + font.width(font.plainSubstrByWidth(value.substring(0, Math.min(getCursorPosition(), value.length())), getInnerWidth()));
            if ((System.currentTimeMillis() / 300L) % 2L == 0L) {
                guiGraphics.drawString(font, "_", Math.min(cursorX, right - 6), textY, 0xFFE0E0E0);
            }
        }
    }
}
