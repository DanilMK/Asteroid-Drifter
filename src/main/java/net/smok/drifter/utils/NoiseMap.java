package net.smok.drifter.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public interface NoiseMap {
    double apply(BlockPos blockPos);

    interface NoiseMapFactory {
        NoiseMap create(RandomSource randomSource, BlockPos origin, Integer size, float fraction);
    }
}
