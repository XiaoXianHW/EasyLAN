package org.xiaoxian.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Field;

import static org.xiaoxian.util.DrawUtil.drawLine;

public class TextBoxUtil extends EditBox {
    private static final String[] DISPLAY_POS_FIELDS = { "displayPos", "f_94100_" };

    private final Field displayPosField;
    private long lastUpdateTick = System.currentTimeMillis();

    public TextBoxUtil(Font font, int x, int y, int width, int height, String msg) {
        super(font, x, y, width, height, Component.nullToEmpty(msg));
        displayPosField = resolveDisplayPosField();
        if (displayPosField != null) {
            displayPosField.setAccessible(true);
        }
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            matrixStack.fill(getX(), getY(), getX() + width + 4, getY() + height, new Color(128, 128, 128, 30).getRGB());

            RenderSystem.lineWidth(2f);
            drawLine(getX(), getX() + width + 3, getY() + height - 1, new Color(135, 206, 250).getRGB());
            RenderSystem.lineWidth(1f);

            String textToDraw = getValue().substring(Math.max(0, getDisplayPos()));
            if (this.isFocused()) {
                long currentTick = System.currentTimeMillis();
                if (currentTick - lastUpdateTick > 10) {
                    textToDraw += "_";
                    lastUpdateTick = currentTick;
                }
            }

            matrixStack.drawString(Minecraft.getInstance().font, textToDraw, getX() + 4, getY() + (height - 8) / 2, this.isFocused() ? 0xFFE0E0E0 : 0xFF707070);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!this.visible) {
            return false;
        }

        boolean hovered = mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= this.getY() && mouseY < this.getY() + this.height;
        this.setFocused(hovered);

        if (this.isFocused() && hovered && mouseButton == 0) {
            this.clicked(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private Field resolveDisplayPosField() {
        for (String fieldName : DISPLAY_POS_FIELDS) {
            try {
                return EditBox.class.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private int getDisplayPos() {
        if (displayPosField == null) {
            return 0;
        }

        try {
            return (int) displayPosField.get(this);
        } catch (IllegalAccessException ignored) {
            return 0;
        }
    }
}
