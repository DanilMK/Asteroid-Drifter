package net.smok.drifter.blocks.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ShipStructure {

    private final HashMap<BlockPos, ShipStructureBlockEntity> structureBlocks = new HashMap<>();

    private @Nullable ShipControllerBlockEntity controllerBlock;

    public static @NotNull Optional<ShipStructure> findStructure(@NotNull Level level, @NotNull BlockPos pos) {
        return ShipStructureBlockEntity.findStructure(level, pos);
    }


    public ShipStructureBlockEntity put(@NotNull BlockPos key, @NotNull ShipStructureBlockEntity value) {
        return structureBlocks.put(key, value);
    }

    public ShipStructureBlockEntity remove(@NotNull BlockPos key) {
        return structureBlocks.remove(key);
    }

    public ShipStructureBlockEntity get(@NotNull BlockPos key) {
        return structureBlocks.get(key);
    }

    public void updateShipBlocks(HashMap<BlockPos, ShipBlock> otherShipBlocks) {
        structureBlocks.forEach((blockPos, shipStructureBlock) -> shipStructureBlock.getShipBlocks()
                .forEach((blockPos1, shipBlock) -> otherShipBlocks.forEach(shipBlock::bind)));
    }

    public HashMap<BlockPos, ShipStructureBlockEntity> getStructureBlocks() {
        return structureBlocks;
    }

    public Iterable<BlockPos> getRandomInAnyBox(@NotNull RandomSource randomSource, int amount) {
        List<ShipStructureBlockEntity> list = structureBlocks.values().stream().toList();
        ShipStructureBlockEntity shipStructureBlockEntity = list.get(randomSource.nextInt(list.size()));
        return getRandomInBox(randomSource, amount, shipStructureBlockEntity);
    }

    public BlockPos getBigBoxMin() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int i = 0;
        for (ShipStructureBlockEntity block : structureBlocks.values()) {
            if (i == 0) pos = block.getBoxMin().mutable();
            else {
                BlockPos cur = block.getBoxMin();
                pos.setX(Math.min(pos.getX(), cur.getX()));
                pos.setY(Math.min(pos.getY(), cur.getY()));
                pos.setZ(Math.min(pos.getZ(), cur.getZ()));
            }
            i++;
        }
        return pos.immutable();
    }

    public BlockPos getBigBoxMax() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int i = 0;
        for (ShipStructureBlockEntity block : structureBlocks.values()) {
            if (i == 0) pos = block.getBoxMax().mutable();
            else {
                BlockPos cur = block.getBoxMax();
                pos.setX(Math.max(pos.getX(), cur.getX()));
                pos.setY(Math.max(pos.getY(), cur.getY()));
                pos.setZ(Math.max(pos.getZ(), cur.getZ()));
            }
            i++;
        }
        return pos.immutable();
    }

    public Optional<ShipControllerBlockEntity> getControllerBlock() {
        if (controllerBlock != null && !controllerBlock.isRemoved()) return Optional.of(controllerBlock);
        return Optional.ofNullable(controllerBlock = findShipControllerBlock());
    }

    public @Nullable ShipControllerBlockEntity findShipControllerBlock() {
        for (ShipStructureBlockEntity shipStructureBlockEntity : structureBlocks.values()) {
            for (ShipBlock shipBlock : shipStructureBlockEntity.getShipBlocks().values()) {
                if (shipBlock instanceof ShipControllerBlockEntity shipControllerBlock) return shipControllerBlock;
            }
        }
        return null;
    }
    /*

    public Iterable<BlockPos> getRandomInAllBoxes(@NotNull RandomSource randomSource, int boxes, int minInBoxes, int maxInBoxes) {
        List<ShipStructureBlockEntity> list = structureBlocks.values().stream().toList();
        return () -> new AbstractIterator<>() {
            int counter = boxes;

            @Override
            protected BlockPos computeNext() {
                if (this.counter <= 0) {
                    return this.endOfData();
                } else {
                    ShipStructureBlockEntity structureBlockEntity = list.get(randomSource.nextInt(list.size()));
                    BlockPos blockPos = getRandomInBox(randomSource, randomSource.nextInt(minInBoxes, maxInBoxes), structureBlockEntity);
                    this.counter--;
                    return blockPos;
                }
            }
        };

    }*/

    private static @NotNull Iterable<BlockPos> getRandomInBox(@NotNull RandomSource randomSource, int amount, ShipStructureBlockEntity shipStructureBlockEntity) {
        BlockPos min = shipStructureBlockEntity.getBoxMin();
        BlockPos max = shipStructureBlockEntity.getBoxMax();
        return BlockPos.randomBetweenClosed(randomSource, amount, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }
}
