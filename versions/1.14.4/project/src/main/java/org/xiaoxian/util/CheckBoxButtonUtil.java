package org.xiaoxian.util;

import net.minecraft.client.Minecraft;

public class CheckBoxButtonUtil extends ButtonUtil {
    private boolean isChecked;

    public CheckBoxButtonUtil(int x, int y, boolean isChecked, int width, int height) {
        super(x, y, width, height, "");
        this.isChecked = isChecked;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        int left = this.x + 4;
        int top = this.y + 4;
        int right = this.x + this.width - 4;
        int bottom = this.y + this.height - 4;
        fill(left, top, right, bottom, this.isChecked ? 0xFFF2F2F2 : 0xA0555555);

        if (this.isChecked) {
            this.drawCenteredString(Minecraft.getInstance().font, "x", this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFF2B2B2B);
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggleChecked() {
        this.isChecked = !this.isChecked;
    }
}
