package org.xiaoxian;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import org.xiaoxian.gui.GuiEasyLanMain;

import java.util.Set;

public class HookConfigFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiEasyLanMain.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
