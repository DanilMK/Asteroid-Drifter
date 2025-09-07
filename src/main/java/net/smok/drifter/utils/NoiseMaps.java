package net.smok.drifter.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.joml.SimplexNoise;

public enum NoiseMaps {
    WHITE(NoiseMaps::white),
    SIMPLEX(NoiseMaps::simplex),
    SPLATTER(NoiseMaps::splatter),
    META_BALL(MetaBalls::createBallsNoise);

    public final NoiseMap.NoiseMapFactory genNoise;

    NoiseMaps(NoiseMap.NoiseMapFactory genNoise) {
        this.genNoise = genNoise;
    }

    private static NoiseMap white(RandomSource randomSource, BlockPos origin, Integer size, float fraction) {
        PositionalRandomFactory factory = randomSource.forkPositional();

        return blockPos -> factory.at(blockPos).nextFloat();
    }

    private static NoiseMap simplex(RandomSource randomSource, BlockPos origin, Integer size, float fraction) {
        int x = randomSource.nextInt(size);
        int y = randomSource.nextInt(size);
        int z = randomSource.nextInt(size);

        return blockPos -> SimplexNoise.noise(
                ((float) blockPos.getX() / size + x) * fraction,
                ((float) blockPos.getY() / size + y) * fraction,
                ((float) blockPos.getZ() / size + z) * fraction) / 2 + 0.5f;
    }

    private static NoiseMap splatter(RandomSource randomSource, BlockPos origin, Integer size, float fraction) {
        NoiseMap white = white(randomSource, origin, size, fraction);
        NoiseMap simplex = simplex(randomSource, origin, size, fraction);

        return blockPos -> (white.apply(blockPos) + simplex.apply(blockPos)) / 2;
    }
}
