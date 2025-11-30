package net.smok.drifter.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.blocks.garden.MoonFarmBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SaplingBlock.class)
public class SaplingMixin {

    @Redirect(
            method = "randomTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int randomTickInject(RandomSource randomSource, int i, @Local(argsOnly = true) BlockState cropState,
                                 @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos cropPos) {
        float v = MoonFarmBlockEntity.getSpeedForCrop(cropState.getBlock(), level, cropPos.below());
        if (v == 0) return 1;
        if (v > 0) return randomSource.nextInt((int) (1f / v) + 1);
        return randomSource.nextInt(i);
    }
}
