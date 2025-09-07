package net.smok.drifter.world.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.smok.drifter.registries.ModifierRegistries;
import net.smok.drifter.utils.NoiseMap;
import net.smok.drifter.utils.NoiseMaps;
import net.smok.drifter.utils.WorldGenUtils;
import net.smok.drifter.world.AsteroidFeature;
import net.smok.drifter.world.Modifier;
import net.smok.drifter.world.ModifierType;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public record NoiseModifier(NoiseMaps noise, boolean testAllCube, float fraction, List<TargetBlockState> blocks) implements Modifier {

    /*

      {
        "asteroid_modifier_type": "asteroid_drifter:noise",
        "noise": "simplex",
        "threshold": 0.5,
        "fraction": 0.2,
        "block": {
          "type": "minecraft:simple_state_provider",
          "state": {
            "Name": "minecraft:stone"
          }
        }
      }
     */

    public static final Codec<NoiseModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("noise").xmap(s -> NoiseMaps.valueOf(s.toUpperCase()), noiseMaps -> noiseMaps.name().toLowerCase()).forGetter(NoiseModifier::noise),
            Codec.BOOL.optionalFieldOf("test_all_cube", false).forGetter(NoiseModifier::testAllCube),
            Codec.FLOAT.fieldOf("fraction").forGetter(NoiseModifier::fraction),
            TargetBlockState.CODEC.listOf().fieldOf("blocks").forGetter(NoiseModifier::blocks)
    ).apply(instance, NoiseModifier::new));


    @Override
    public void apply(AsteroidFeature.GenerationContext context) {
        NoiseMap noiseMap = noise.genNoise.create(context.randomSource(), context.origin(), context.config().size(), fraction);

        HashMap<BlockPos, BlockState> blocks = new HashMap<>(context.blocks());
        if (testAllCube) {
            WorldGenUtils.forEachCube(context.origin(), context.config().size() / 2, pos ->
                    replaceBlock(context, pos, noiseMap, blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState())));
        } else {
            blocks.forEach((pos, blockState) ->
                    replaceBlock(context, pos, noiseMap, blockState));
        }

    }

    private void replaceBlock(AsteroidFeature.GenerationContext context, BlockPos blockPos, NoiseMap noiseMap, BlockState blockState) {
        double v = noiseMap.apply(blockPos);
        getBlock(v, blockState, context.randomSource()).ifPresent(block -> context.blocks().put(blockPos, block));
    }

    private Optional<BlockState> getBlock(double v, BlockState previousState, RandomSource randomSource) {
        Optional<TargetBlockState> first = blocks.stream().filter(
                target -> target.threshold < v && target.target.test(previousState, randomSource)).findFirst();
        return first.map(pair -> pair.block);
    }

    private static BlockState getBlockTest(double v) {
        if (v > .9) return Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
        if (v > .8) return Blocks.ANDESITE.defaultBlockState();
        if (v > .7) return Blocks.STONE.defaultBlockState();
        if (v > .6) return Blocks.TUFF.defaultBlockState();
        if (v > .5) return Blocks.CYAN_TERRACOTTA.defaultBlockState();
        if (v > .4) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        if (v > .3) return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        if (v > .2) return Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState();
        if (v > .1) return Blocks.SCULK.defaultBlockState();
        return Blocks.GLASS.defaultBlockState();
    }

    @Override
    public ModifierType<?> getType() {
        return ModifierRegistries.NOISE;
    }


    public record TargetBlockState(float threshold, RuleTest target, BlockState block) {

        private static final Codec<TargetBlockState> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.fieldOf("threshold").forGetter(TargetBlockState::threshold),
                RuleTest.CODEC.optionalFieldOf("target", AlwaysTrueTest.INSTANCE).forGetter(TargetBlockState::target),
                BlockState.CODEC.fieldOf("state").forGetter(TargetBlockState::block)
        ).apply(i, TargetBlockState::new));
    }


}
