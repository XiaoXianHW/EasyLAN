package org.xiaoxian.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.lang.reflect.Field;

import static org.xiaoxian.util.DrawUtil.drawLine;

public class TextBoxUtil extends TextFieldWidget {
    private Field lineScrollOffsetField;
    private long lastUpdateTick = 20;

    public TextBoxUtil(FontRenderer fontRenderer, int x, int y, int width, int height, String msg) {
        super(fontRenderer, x, y, width, height, ITextComponent.getTextComponentOrEmpty(msg));
        lineScrollOffsetField = resolveLineScrollOffsetField();
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            fill(matrixStack, x, y, x + width + 4, y + height, new Color(128, 128, 128, 30).getRGB());
            RenderSystem.lineWidth(2f);
            drawLine(x, x + width + 3, y + height - 1, new Color(135, 206, 250).getRGB());
            RenderSystem.lineWidth(1f);
            int textColor = this.isFocused() ? 14737632 : 7368816;

            int lineScrollOffset = 0;
            if (lineScrollOffsetField != null) {
                try {
                    lineScrollOffset = (int) lineScrollOffsetField.get(this);
                } catch (IllegalAccessException ex) {
                    System.out.println("[EasyLAN | TextBoxUtil] Error: " + ex.getMessage());
                }
            }
            String textToDraw = getText().substring(Math.max(0, lineScrollOffset));

            if (this.isFocused()) {
                long currentTick = System.currentTimeMillis();
                if (currentTick - lastUpdateTick > 10) {
                    textToDraw += "_";
                    lastUpdateTick = currentTick;
                }
            }

            Minecraft.getInstance().fontRenderer.drawStringWithShadow(matrixStack, textToDraw, x + 4, y + (float) (height - 8) / 2, textColor);
        }
    }

    private Field resolveLineScrollOffsetField() {
        String[] candidates = new String[] { "displayPos", "field_146225_q" };
        for (String candidate : candidates) {
            try {
                Field field = TextFieldWidget.class.getDeclaredField(candidate);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
