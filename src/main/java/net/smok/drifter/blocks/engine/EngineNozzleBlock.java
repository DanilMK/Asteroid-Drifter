package net.smok.drifter.blocks.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.smok.drifter.ShipConfig;
import net.smok.drifter.blocks.ShipBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EngineNozzleBlock extends DirectionalBlock implements ShipBlock {


    public static final Property<Boolean> LIT = BlockStateProperties.LIT;
    private final float maxSpeedFactor;

    public EngineNozzleBlock(Properties properties, float maxSpeedFactor) {
        super(properties);
        this.maxSpeedFactor = maxSpeedFactor;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }


    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }


    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(LIT);
    }


    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    public float getMaxSpeed() {
        return maxSpeedFactor * ShipConfig.startSpeed();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            Direction direction = state.getValue(FACING);
            Vec3 particlePoint = pos.relative(direction).getCenter();
            Vec3i directionNormal = direction.getNormal();
            float v = random.nextFloat();

            level.addParticle(ParticleTypes.DRAGON_BREATH,
                    particlePoint.x + random.nextFloat() - 0.5,
                    particlePoint.y + random.nextFloat() - 0.5,
                    particlePoint.z + random.nextFloat() - 0.5,
                    directionNormal.getX() * 2 + (v - 0.5) / 2,
                    directionNormal.getY() * 2 + (v - 0.5) / 2,
                    directionNormal.getZ() * 2 + (v - 0.5) / 2
            );

        }
    }
}
