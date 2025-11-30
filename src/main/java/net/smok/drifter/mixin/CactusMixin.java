package net.smok.drifter.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.blocks.garden.MoonFarmBlockEntity;
import net.smok.drifter.registries.DrifterBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CactusBlock.class)
public class CactusMixin {


    @Redirect(method = "canSurvive",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean canSurviveRedirect(BlockState state, TagKey<Block> tagKey, @Local BlockPos cropPos, @Local LevelReader level) {
        Optional<MoonFarmBlockEntity> blockEntity =
                level.getBlockEntity(cropPos.below(), DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get());
        return blockEntity.map(farmland -> farmland.canSurvive((Block) (Object) this)).orElse(state.is(tagKey));
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
