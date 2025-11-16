package net.smok.drifter.mixin;

import earth.terrarium.adastra.api.systems.OxygenApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.smok.drifter.DrifterRespawnLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Change spawn logic
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "fudgeSpawnLocation", at = @At("HEAD"), cancellable = true)
    private void fudgeSpawnLocationInjection(ServerLevel level, CallbackInfo ci) {
        if (!OxygenApi.API.hasOxygen(level)) {
            ci.cancel();
            ServerPlayer player = (ServerPlayer) (Object) this;
            BlockPos blockPos = DrifterRespawnLogic.fudgeSpawnLocationInjection(level, player);
            player.moveTo(blockPos, 0, 0);
        }
    }

}
