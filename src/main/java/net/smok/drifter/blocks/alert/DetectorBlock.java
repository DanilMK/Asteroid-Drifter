package net.smok.drifter.blocks.alert;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.smok.drifter.blocks.BasicEntityBlock;
import net.smok.drifter.registries.DrifterBlocks;
import org.jetbrains.annotations.Nullable;

public class DetectorBlock extends BasicEntityBlock {


    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public DetectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;
        level.getBlockEntity(pos, DrifterBlocks.DETECTOR_BLOCK_ENTITY.get()).ifPresent(
                detector -> detector.checkAndActivate(state, pos)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DetectorBlockEntity(pos, state);
    }
}
