package org.xiaoxian.util;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nonnull;
import java.awt.*;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean isChecked;

    public CheckBoxButtonUtil(int x, int y, boolean isChecked, int width, int height) {
        super(x, y, width, height, "");
        this.isChecked = isChecked;
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        Color color = this.isChecked ? Color.WHITE: Color.GRAY;
        DrawUtil.drawRect(this.x + 2, this.y + 2, this.width - 4, this.height - 4, color);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }
}
