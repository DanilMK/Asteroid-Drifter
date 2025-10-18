package net.smok.drifter.blocks.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.blocks.BasicEntityBlock;
import net.smok.drifter.registries.DrifterBlocks;
import org.jetbrains.annotations.Nullable;

public class ShipStructureBlock extends BasicEntityBlock {
    public ShipStructureBlock(Properties properties) {
        super(properties);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShipStructureBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == DrifterBlocks.SHIP_STRUCTURE_BLOCK_ENTITY.get() ?
                (level1, blockPos, blockState, blockEntity) -> {
            ((ShipStructureBlockEntity)blockEntity).tick(level1.getGameTime());
        } : null;
    }
}
