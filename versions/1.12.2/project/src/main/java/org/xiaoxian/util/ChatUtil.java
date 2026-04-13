package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

import java.util.regex.Pattern;

public class ChatUtil {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final Pattern PATTERN = Pattern.compile("&([0-9a-fk-or])");

    public static void sendMsg(String msg) {
        msg = PATTERN.matcher(msg).replaceAll("\u00A7$1");
        MC.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(msg));
    }
}
