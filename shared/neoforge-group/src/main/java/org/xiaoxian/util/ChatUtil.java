package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.regex.Pattern;

public class ChatUtil {
    private static final Pattern PATTERN = Pattern.compile("&([0-9a-fk-or])");

    public static void sendMsg(String msg) {
        sendComponentMsg(Component.nullToEmpty(PATTERN.matcher(msg).replaceAll("\u00A7$1")));
    }

    public static void sendComponentMsg(Component component) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        // Chat lines may be pushed from the server thread, the chat component is client thread only.
        minecraft.execute(() -> {
            if (minecraft.gui != null) {
                minecraft.gui.getChat().addMessage(component);
            }
        });
    }
}
