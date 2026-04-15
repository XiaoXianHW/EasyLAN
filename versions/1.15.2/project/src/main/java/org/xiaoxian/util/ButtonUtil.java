package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;

public class ButtonUtil extends Button {
    public ButtonUtil(int x, int y, int width, int height, String buttonText) {
        super(x, y, width, height, buttonText, button -> {});
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int backgroundColor = isHovered() ? 0x80808080 : 0x40404080;
        fill(this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);

        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
    }
}
