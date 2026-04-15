package org.xiaoxian.util;

import com.mojang.blaze3d.vertex.PoseStack;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean checked;

    public CheckBoxButtonUtil(int x, int y, boolean checked, int width, int height) {
        super(x, y, width, height, "");
        this.checked = checked;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        fill(matrixStack, this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height - 2, this.checked ? 0xFFFFFFFF : 0xFF7F7F7F);
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }
}
