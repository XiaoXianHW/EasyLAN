package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean isChecked;

    public CheckBoxButtonUtil(int x, int y, boolean isChecked, int width, int height) {
        super(ButtonUtil.builder(x, y, width, height, ""));
        this.isChecked = isChecked;
    }

    @Override
    public void renderWidget(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        int left = this.getX() + 4;
        int top = this.getY() + 4;
        int right = this.getX() + this.width - 4;
        int bottom = this.getY() + this.height - 4;
        matrixStack.fill(left, top, right, bottom, this.isChecked ? 0xFFF2F2F2 : 0xA0555555);

        if (this.isChecked) {
            matrixStack.drawCenteredString(Minecraft.getInstance().font, "x", this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFF2B2B2B);
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }
}
