package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import com.mojang.blaze3d.platform.GlStateManager;

import java.awt.Color;

public class ButtonUtil extends Button {

    public ButtonUtil(int x, int y, int width, int height, String buttonText) {
        super(x, y, width, height, buttonText, button -> {});
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            GlStateManager.enableBlend();
            Color color;
            if (isHovered()) {
                color = new Color(128, 128, 128, 128); // 灰色，半透明
            } else {
                color = new Color(64, 64, 64, 128); // 深灰色，半透明
            }
            fill(this.x, this.y, this.x + this.width, this.y + this.height, color.getRGB());
            GlStateManager.disableBlend();

            this.drawCenteredString(Minecraft.getInstance().fontRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
        }
    }
}
