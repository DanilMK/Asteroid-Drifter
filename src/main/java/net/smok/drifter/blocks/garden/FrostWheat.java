package net.smok.drifter.blocks.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FrostWheat extends CropBlock{

    public FrostWheat(Properties properties) {
        super(7, EIGHT_AGE_SHAPES, properties);
    }


    @Override
    protected void growByOne(Level level, BlockState state, BlockPos pos) {
        super.growByOne(level, state, pos);

        int shiftX = level.random.nextInt(5);
        int shiftZ = level.random.nextInt(5);
        int yMin = pos.getY() - 3;

        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                ;
                for (BlockPos.MutableBlockPos point = pos.offset((x + shiftX) % 5, 3, (z + shiftZ) % 5).mutable();
                     point.getY() > yMin; point.move(Direction.DOWN)) {

                    if (level.getBlockState(point).isAir() && Blocks.SNOW.canSurvive(Blocks.SNOW.defaultBlockState(), level, point)) {
                        level.setBlock(point, Blocks.SNOW.defaultBlockState(), 2);
                        return;
                    }
                }
            }
        }
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (icy(state) && random.nextFloat() < 0.25f) {
            double d = pos.getX() + random.nextDouble();
            double e = pos.getY() + random.nextDouble();
            double f = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, d, e, f,
                    Mth.randomBetween(random, -1.0F, 1.0F) * 0.083333336F,
                    0.05F,
                    Mth.randomBetween(random, -1.0F, 1.0F) * 0.083333336F);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (icy(state))
            Blocks.POWDER_SNOW.entityInside(state, level, pos, entity);
    }

    private boolean icy(BlockState state) {
        return getAge(state) > getMaxAge() / 2;
    }
}
