package net.smok.drifter.blocks;

import net.minecraft.core.BlockPos;

public interface ShipBlock {

    default boolean bind(BlockPos pos, ShipBlock other) {
        return false;
    }

    default void clear() {}
}
