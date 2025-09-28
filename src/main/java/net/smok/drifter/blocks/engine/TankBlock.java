package net.smok.drifter.blocks.engine;

import earth.terrarium.botarium.common.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.smok.drifter.utils.ExtraUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TankBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = box(1, 0, 1, 15, 16, 15);
    private final long containerAmount;

    public TankBlock(Properties properties, long millibucketsAmount) {
        super(properties);
        this.containerAmount = FluidConstants.fromMillibuckets(millibucketsAmount);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ExtraUtils.handleBucketUseOnTank(level, pos, player);
    }


    public long getContainerAmount() {
        return containerAmount;
    }
}
