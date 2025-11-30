package net.smok.drifter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.blocks.garden.MoonFarmBlockEntity;
import net.smok.drifter.registries.DrifterBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneBlockMixin {

    @Inject(
            method = "canSurvive",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
                    ordinal = 1),
            cancellable = true)
    private void canSurviveInjection(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        level.getBlockEntity(pos, DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get()).ifPresent(highFarmlandBlockEntity ->
                cir.setReturnValue(highFarmlandBlockEntity.canSurvive((Block) (Object) this)));
    }

    @Inject(
            method = "randomTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void randomTickInjection(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        float v = MoonFarmBlockEntity.getSpeedForCrop(state.getBlock(), level, pos);
        if (v == 0) ci.cancel();
        if (v > 0 && random.nextInt((int) (1 / v) + 1) != 0) ci.cancel();
    }
}
