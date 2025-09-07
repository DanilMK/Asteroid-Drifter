package net.smok.drifter.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.Debug;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class WorldGenUtils {


    public static void sharpSurface(HashMap<BlockPos, BlockState> placedBlocks, RandomSource random, float sharpness) {
        Debug.log("Sharp start " + sharpness);
        var setCopy = new HashMap<>(placedBlocks);
        setCopy.keySet().forEach(blockPos -> {

            AtomicInteger c = new AtomicInteger();
            forEachNeighbor(blockPos, p -> {
                if (setCopy.containsKey(p)) c.getAndIncrement();
            });

            if (c.get() < 6 && random.nextFloat() * 2 < sharpness) {
                forEachNeighbor(blockPos, placedBlocks::remove);
                placedBlocks.remove(blockPos);
            }
        });
    }

    public static void smooth(int smooth, HashMap<BlockPos, BlockState> placedBlocks) {
        Debug.log("Smooth surface start");
        int smoothCube = (smooth + 1) * (smooth + 1) * (smooth + 1);
        var setCopy = new HashMap<>(placedBlocks);
        setCopy.forEach((blockPos, blockState) -> smoothApply(smooth, smoothCube, placedBlocks, setCopy, blockPos));
    }

    public static void smoothApply(int smooth, int smoothCube, HashMap<BlockPos, BlockState> placedBlocks, HashMap<BlockPos, BlockState> setCopy, BlockPos pos) {
        AtomicInteger c = new AtomicInteger();

        forEachCube(pos, smooth, p -> {
            if (setCopy.containsKey(p)) c.getAndIncrement();
        });

        if (c.get() < smoothCube) placedBlocks.remove(pos);
    }

    public static void forEachInBound(@NotNull BlockPos min, @NotNull BlockPos max, Consumer<BlockPos> action) {
        for (int x = min.getX(); x < max.getX(); x++) {
            for (int y = min.getY(); y < max.getY(); y++) {
                for (int z = min.getZ(); z < max.getZ(); z++) {
                    action.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    public static void forEachCube(@NotNull BlockPos origin, int size, Consumer<BlockPos> action) {
        for (int x = origin.getX() - size; x < origin.getX() + size; x++) {
            for (int y = origin.getY() - size; y < origin.getY() + size; y++) {
                for (int z = origin.getZ() - size; z < origin.getZ() + size; z++) {
                    action.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    public static void forEachNeighbor(BlockPos origin, Consumer<BlockPos> action) {
        action.accept(origin.above());
        action.accept(origin.below());
        action.accept(origin.north());
        action.accept(origin.south());
        action.accept(origin.west());
        action.accept(origin.east());
    }

    @Contract("_ -> new")
    private static BlockPos @NotNull [] getNeighbors(BlockPos blockPos) {
        return new BlockPos[] {
                blockPos.above(),
                blockPos.below(),
                blockPos.north(),
                blockPos.south(),
                blockPos.west(),
                blockPos.east()
        };
    }
}
