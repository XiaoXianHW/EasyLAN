package org.xiaoxian.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class ChatUtil {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Pattern PATTERN = Pattern.compile("&([0-9a-fk-or])");

    public static void sendMsg(String msg) {
        msg = PATTERN.matcher(msg).replaceAll("\u00A7$1");
        MC.gui.getChat().addMessage(createLiteralComponent(msg));
    }

    private static Component createLiteralComponent(String msg) {
        try {
            Method method = Component.class.getMethod("literal", String.class);
            Object value = method.invoke(null, msg);
            if (value instanceof Component component) {
                return component;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Class<?> clazz = Class.forName("net.minecraft.network.chat.TextComponent");
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object value = constructor.newInstance(msg);
            if (value instanceof Component component) {
                return component;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return Component.nullToEmpty(msg);
    }
}
