package org.xiaoxian.util;

import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.awt.*;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean isChecked;

    public CheckBoxButtonUtil(int x, int y, boolean isChecked, int width, int height) {
        super(ButtonUtil.builder(x, y, width, height, ""));
        this.isChecked = isChecked;
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);

        Color color = this.isChecked ? Color.WHITE: Color.GRAY;
        DrawUtil.drawRect(this.getX() + 2, this.getY() + 2, this.width - 4, this.height - 4, color);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }
}
