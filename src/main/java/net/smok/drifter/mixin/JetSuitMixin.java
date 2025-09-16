package net.smok.drifter.mixin;

import earth.terrarium.adastra.common.items.armor.JetSuitItem;
import earth.terrarium.adastra.common.utils.KeybindManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JetSuitItem.class)
public abstract class JetSuitMixin {


    @Shadow protected abstract boolean canFly(Player player, ItemStack stack);

    @Shadow protected abstract void consume(Player player, ItemStack stack, int amount, int slotId);

    @Inject(method = "inventoryTick",
            at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/common/utils/KeybindManager;jumpDown(Lnet/minecraft/world/entity/player/Player;)Z"),
    cancellable = true)
    private void downFlight(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo callbackInfo) {
        Player player = (Player) entity;
        if (!player.isShiftKeyDown() || !canFly(player, stack)) return;
        callbackInfo.cancel();
        if (KeybindManager.jumpDown(player)) {
            Vec3 delta = player.getDeltaMovement();
            double x = 0, y = 0, z = 0;
            x = delta.x < 0.0000001 ? 0 : delta.x * .9;
            y = delta.y < 0.0000001 ? 0 : delta.y * .9;
            z = delta.z < 0.0000001 ? 0 : delta.z * .9;
            player.setDeltaMovement(new Vec3(x, y, z));
        } else {
            double acceleration = JetSuitItem.sigmoidAcceleration(player.tickCount, 5.0, 1.0, 2.0);
            acceleration /= 25.0;
            player.addDeltaMovement(new Vec3(0.0, -Math.max(0.0025, acceleration), 0.0));
        }
        player.fallDistance = Math.max(player.fallDistance / 1.5F, 0.0F);
        if (!player.onGround()) consume(player, stack, 50, slotId);
    }

    @Redirect(method = "spawnParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/item/ItemStack;)V",
    at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/common/utils/KeybindManager;jumpDown(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean extendParticles(Player player) {
        return KeybindManager.jumpDown(player) || (player.isShiftKeyDown() && !player.onGround());
    }
}
