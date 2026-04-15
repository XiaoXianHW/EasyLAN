package org.xiaoxian.util;

import com.mojang.blaze3d.vertex.PoseStack;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean checked;

    public CheckBoxButtonUtil(int x, int y, boolean checked, int width, int height) {
        super(x, y, width, height, "");
        this.checked = checked;
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        int color = this.checked ? 0xFFFFFFFF : 0xFF7F7F7F;
        fill(matrixStack, this.getX() + 2, this.getY() + 2, this.getX() + this.width - 2, this.getY() + this.height - 2, color);
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }
}
