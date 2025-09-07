package net.smok.drifter.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.smok.drifter.Debug;
import net.smok.drifter.utils.MetaBalls;
import net.smok.drifter.utils.WorldGenUtils;
import net.smok.drifter.utils.NoiseMaps;

import java.util.*;

public class AsteroidFeature extends Feature<AsteroidFeatureConfiguration> {

    public AsteroidFeature() {
        super(AsteroidFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<AsteroidFeatureConfiguration> context) {
        boolean b;
        try {
            b = run(context);
        } catch (Exception e) {
            Debug.err("Asteroid Feature cannot be place.", e);
            return false;
        }

        return b;
    }

    private boolean run(FeaturePlaceContext<AsteroidFeatureConfiguration> context) {
        BlockStateConfiguration filler = context.config().filler();
        int size = context.config().size() / 2;
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();




        var dots = NoiseMaps.SIMPLEX.genNoise.create(random, origin, size, context.config().fraction());
        MetaBalls.Ball ball = new MetaBalls.Ball(size, origin);

        HashMap<BlockPos, BlockState> placedBlocks = new HashMap<>();
        WorldGenUtils.forEachCube(origin, size * 2, pos -> {

            double density = dots.apply(pos) * ball.apply(pos);
            if (density < 0.5) return;
            placedBlocks.put(pos, filler.state);
        });
        if (placedBlocks.isEmpty()) return false;

        if (context.config().hasModifiers()) {
            GenerationContext context1 = new GenerationContext(origin, context.config(), random, placedBlocks);
            context.config().modifiers().forEach(modifierConfig -> modifierConfig.apply(context1));
        }


        WorldGenUtils.forEachCube(origin, size * 2, pos -> {
            if (placedBlocks.containsKey(pos)) setBlock(level, pos, placedBlocks.get(pos));
            else setBlock(level, pos, Blocks.AIR.defaultBlockState());
        });
        return true;
    }


    public record GenerationContext(BlockPos origin, AsteroidFeatureConfiguration config,
                                    RandomSource randomSource, HashMap<BlockPos, BlockState> blocks) {}

}
