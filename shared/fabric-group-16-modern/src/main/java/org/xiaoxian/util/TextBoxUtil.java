package org.xiaoxian.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

public class TextBoxUtil extends EditBox {
    public TextBoxUtil(Font font, int x, int y, int width, int height, String msg) {
        super(font, x, y, width, height, new TextComponent(msg == null ? "" : msg));
    }
}
