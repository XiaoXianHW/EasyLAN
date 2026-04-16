package org.xiaoxian.easylan.fabric.mixin;

import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.xiaoxian.EasyLAN.CustomMaxPlayer;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Inject(method = "getMaxPlayers", at = @At("HEAD"), cancellable = true)
    private void easylan$overrideMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        if (CustomMaxPlayer == null || CustomMaxPlayer.isEmpty()) {
            return;
        }

        try {
            int maxPlayers = Integer.parseInt(CustomMaxPlayer);
            if (maxPlayers >= 2 && maxPlayers <= 500000) {
                cir.setReturnValue(maxPlayers);
            }
        } catch (NumberFormatException ignored) {
        }
    }
}
