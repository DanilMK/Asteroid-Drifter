package net.smok.drifter.blocks.alert;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.smok.drifter.registries.DrifterBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlertLampBlock extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);

    private final LampShape lampShape;

    public AlertLampBlock(LampShape lampShape, Properties properties) {
        super(properties);
        this.lampShape = lampShape;
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.WALL)
                .setValue(COLOR, 0));
    }


    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return lampShape.getShape(blockState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = super.getStateForPlacement(blockPlaceContext);
        if (blockState == null) return null;
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();

        return blockState.setValue(COLOR, level.getBestNeighborSignal(blockPos));
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.isClientSide) return;

        Optional<AlertLampBlockEntity> blockEntity = level.getBlockEntity(blockPos, DrifterBlocks.ALERT_LAMP_BLOCK_ENTITY.get());
        int activeColor = blockEntity.map(AlertLampBlockEntity::getActiveColor).orElse(0);

        updateLampColor(blockState, level, blockPos, activeColor);
    }

    public static void updateLampColor(BlockState blockState, Level level, BlockPos blockPos, int activeColor) {
        int signal = level.getBestNeighborSignal(blockPos);
        int currentColor = blockState.getValue(COLOR);
        int nextColor = activeColor > 0 ? activeColor : signal;

        if (nextColor != currentColor) level.setBlock(blockPos, blockState.setValue(COLOR, nextColor), 2);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(COLOR, FACING, FACE));
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AlertLampBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return blockEntityType == DrifterBlocks.ALERT_LAMP_BLOCK_ENTITY.get() ? (level1, blockPos, state, type) ->
                AlertLampBlockEntity.tick(level1, blockPos, state, (AlertLampBlockEntity) type) : null;
    }


    public record LampShape(VoxelShape northShape, VoxelShape eastShape, VoxelShape southShape,
                            VoxelShape westShape, VoxelShape upShapeX, VoxelShape upShapeZ,
                            VoxelShape downShapeX, VoxelShape downShapeZ) {

        public VoxelShape getShape(@NotNull BlockState state) {

            VoxelShape result;
            switch (state.getValue(FACE)) {
                case FLOOR:
                    result = state.getValue(FACING).getAxis() == Direction.Axis.X ? downShapeX : downShapeZ;
                    break;
                case WALL:
                    switch (state.getValue(FACING)) {
                        case EAST:
                            result = eastShape;
                            return result;
                        case SOUTH:
                            result = southShape;
                            return result;
                        case WEST:
                            result = westShape;
                            return result;
                        default:
                            result = northShape;
                            return result;
                    }
                case CEILING:
                    result = state.getValue(FACING).getAxis() == Direction.Axis.X ? upShapeX : upShapeZ;
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }

            return result;
        }
    }
}
