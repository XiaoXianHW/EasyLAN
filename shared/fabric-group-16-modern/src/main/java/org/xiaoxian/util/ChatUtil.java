package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import java.util.regex.Pattern;

public class ChatUtil {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Pattern PATTERN = Pattern.compile("&([0-9a-fk-or])");

    public static void sendMsg(String msg) {
        msg = PATTERN.matcher(msg).replaceAll("\u00A7$1");
        MC.gui.getChat().addMessage(new TextComponent(msg));
    }
}
