package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;

public class ButtonUtil extends Button {
    private static final int NORMAL_BORDER_COLOR = 0x70808080;
    private static final int HOVER_BORDER_COLOR = 0x90A7D8FF;
    private static final int NORMAL_BACKGROUND_COLOR = 0x7A303030;
    private static final int HOVER_BACKGROUND_COLOR = 0x9A4A4A4A;

    public ButtonUtil(int x, int y, int width, int height, String buttonText) {
        super(x, y, width, height, buttonText, button -> {});
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        fill(this.x, this.y, this.x + this.width, this.y + this.height, isHovered() ? HOVER_BORDER_COLOR : NORMAL_BORDER_COLOR);
        fill(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, isHovered() ? HOVER_BACKGROUND_COLOR : NORMAL_BACKGROUND_COLOR);

        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
    }
}
