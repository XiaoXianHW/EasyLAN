package org.xiaoxian.easylan.fabric.mixin;

import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiaoxian.easylan.fabric.version.ConfiguredPlayerLimit;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin implements ConfiguredPlayerLimit {
    @Unique
    private volatile int easylan$maxPlayers;

    @Override
    public void easylan$setMaxPlayers(int maxPlayers) {
        this.easylan$maxPlayers = maxPlayers;
    }

    @Inject(method = "getMaxPlayers()I", at = @At("HEAD"), cancellable = true)
    private void easylan$resolveMaxPlayers(CallbackInfoReturnable<Integer> callback) {
        if (easylan$maxPlayers > 0) {
            callback.setReturnValue(easylan$maxPlayers);
        }
    }
}
