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

import static org.xiaoxian.EasyLAN.devMode;
import static org.xiaoxian.util.DrawUtil.drawLine;

public class TextBoxUtil extends EditBox {

    String fieldName = devMode ? "displayPos" : "f_94100_";
    private Field lineScrollOffsetField;
    private long lastUpdateTick = 20;

    public TextBoxUtil(Font font, int x, int y, int width, int height, String msg) {
        super(font, x, y, width, height, Component.nullToEmpty(msg));

        try {
            lineScrollOffsetField = EditBox.class.getDeclaredField(fieldName);
            lineScrollOffsetField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            matrixStack.fill(getX(), getY(), getX() + width + 4, getY() + height, new Color(128, 128, 128, 30).getRGB());
            RenderSystem.lineWidth(2f);
            drawLine(getX(), getX() + width + 3, getY() + height - 1, new Color(135,206,250).getRGB());
            RenderSystem.lineWidth(1f);
            int textColor = this.isFocused() ? 14737632 : 7368816;

            int lineScrollOffset = 0;
            try {
                lineScrollOffset = (int) lineScrollOffsetField.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            String textToDraw = getValue().substring(Math.max(0, lineScrollOffset));

            if (this.isFocused()) {
                long currentTick = System.currentTimeMillis();
                if (currentTick - lastUpdateTick > 10) {
                    textToDraw += "_";
                    lastUpdateTick = currentTick;
                }
            }

            matrixStack.drawString(Minecraft.getInstance().font, textToDraw, getX() + 4, getY() + (height - 8) / 2, textColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!this.visible) {
            return false;
        } else {
            boolean flag = mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= this.getY() && mouseY < this.getY() + this.height;
            this.setFocused(flag);

            if (this.isFocused() && flag && mouseButton == 0) {
                this.clicked(mouseX, mouseY);
                return true;
            } else {
                return false;
            }
        }
    }

}