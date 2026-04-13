package org.xiaoxian.util;

import java.awt.*;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean isChecked;

    public CheckBoxButtonUtil(int x, int y, boolean isChecked, int width, int height) {
        super(x, y, width, height, "");
        this.isChecked = isChecked;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

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
