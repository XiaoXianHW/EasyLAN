package org.xiaoxian.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean isChecked;

    public CheckBoxButtonUtil(int x, int y, boolean isChecked, int width, int height) {
        super(ButtonUtil.builder(x, y, width, height, ""));
        this.isChecked = isChecked;
    }

    @Override
    protected void renderContents(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        int color = this.isChecked ? 0xFFFFFFFF : 0xFF7F7F7F;
        matrixStack.fill(this.getX() + 4, this.getY() + 4, this.getX() + this.width - 4, this.getY() + this.height - 4, color);
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.isChecked = !this.isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }
}
