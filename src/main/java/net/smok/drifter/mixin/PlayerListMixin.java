package net.smok.drifter.mixin;

import net.minecraft.world.level.Level;
import net.smok.drifter.DrifterRespawnLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

// Change spawn world
// The code belongs to Commoble : https://github.com/Commoble/respawn
@Mixin(PlayerList.class)
public class PlayerListMixin
{
    @SuppressWarnings("rawtypes")
    @Redirect(method="placeNewPlayer", allow=2, require=2, at = @At(value="FIELD", target="Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<Level> redirectPlayerListPlaceNewPlayerGetOverworldImpl()
    {
        return DrifterRespawnLogic.redirectPlayerListPlaceNewPlayerGetOverworld();
    }

    @Inject(method="getPlayerForLogin", at=@At("TAIL"), cancellable=true)
    private void onPlayerListGetPlayerForLoginImpl(GameProfile profile, CallbackInfoReturnable<ServerPlayer> cir)
    {
        DrifterRespawnLogic.onPlayerListGetPlayerForLogin((PlayerList)(Object)this, profile, cir);
    }

    @Redirect(method="respawn", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel redirectPlayerListRespawnGetServerOverworldImpl(MinecraftServer server)
    {
        return DrifterRespawnLogic.redirectPlayerListRespawnGetServerOverworld(server);
    }

}