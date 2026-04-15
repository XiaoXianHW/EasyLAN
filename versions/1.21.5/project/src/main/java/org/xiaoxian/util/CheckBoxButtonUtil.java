package org.xiaoxian.util;

import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.awt.Color;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean checked;

    public CheckBoxButtonUtil(int x, int y, boolean checked, int width, int height) {
        super(ButtonUtil.builder(x, y, width, height, ""));
        this.checked = checked;
    }

    @Override
    protected void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

        int innerColor = checked ? Color.WHITE.getRGB() : new Color(110, 110, 110, 220).getRGB();
        guiGraphics.fill(this.getX() + 3, this.getY() + 3, this.getX() + this.width - 3, this.getY() + this.height - 3, innerColor);
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}
