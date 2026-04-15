package org.xiaoxian.util;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean checked;

    public CheckBoxButtonUtil(int x, int y, boolean checked, int width, int height) {
        super(x, y, width, height, "");
        this.checked = checked;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        DrawUtil.drawRect(this.x + 2, this.y + 2, this.width - 4, this.height - 4, this.checked ? Color.WHITE : Color.GRAY);
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }
}
