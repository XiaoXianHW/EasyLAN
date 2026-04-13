package org.xiaoxian.util;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.awt.*;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean isChecked;

    public CheckBoxButtonUtil(int buttonId, int x, int y, boolean isChecked, int width, int height) {
        super(buttonId, x, y, width, height, "");
        this.isChecked = isChecked;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);

        Color color = this.isChecked ? Color.WHITE: Color.GRAY;
        DrawUtil.drawRect(this.xPosition + 2, this.yPosition + 2, this.width - 4, this.height - 4, color);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
