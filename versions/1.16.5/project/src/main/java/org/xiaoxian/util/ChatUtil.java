package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

import java.util.regex.Pattern;

public class ChatUtil {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Pattern pattern = Pattern.compile("&([0-9a-fk-or])");

    public static void sendMsg(String msg) {
        msg = pattern.matcher(msg).replaceAll("§$1");
        mc.gui.getChat().addMessage(new StringTextComponent(msg));
    }
}
