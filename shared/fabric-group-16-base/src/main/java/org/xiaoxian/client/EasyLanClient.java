package org.xiaoxian.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.Screen;
import org.xiaoxian.EasyLAN;
import org.xiaoxian.gui.GuiShareToLanEdit;
import org.xiaoxian.gui.GuiWorldSelectionEdit;

import java.lang.reflect.Field;

public class EasyLanClient implements ClientModInitializer {
    private Screen lastProcessedScreen;

    @Override
    public void onInitializeClient() {
        GuiShareToLanEdit.PortText = EasyLAN.CustomPort;
        GuiShareToLanEdit.MaxPlayerText = EasyLAN.CustomMaxPlayer;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Screen currentScreen = readCurrentScreen(client);
            if (currentScreen == null || currentScreen == lastProcessedScreen) {
                return;
            }

            lastProcessedScreen = currentScreen;
            GuiWorldSelectionEdit.maybeReplace(client, currentScreen);
            GuiShareToLanEdit.maybeReplace(client, currentScreen);
        });
    }

    private Screen readCurrentScreen(Object client) {
        for (String fieldName : new String[] { "screen", "currentScreen" }) {
            try {
                Field field = client.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(client);
                if (value instanceof Screen) {
                    return (Screen) value;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }
}
