package net.smok.drifter.blocks.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class CropBlock extends BushBlock {

    public static final VoxelShape FULL_BLOCK_SHAPE =
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public static final VoxelShape SAPLING_SHAPE =
            Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public static final VoxelShape[] SHAPES8 = new VoxelShape[]{
            Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
            FULL_BLOCK_SHAPE
    };

    public static final VoxelShape[] SHAPE4 = new VoxelShape[]{
            Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 11.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0)
    };

    public static final Function<Integer, VoxelShape> STABLE_SHAPE = i -> FULL_BLOCK_SHAPE;
    public static final Function<Integer, VoxelShape> STABLE_SAPLING_SHAPE = i -> SAPLING_SHAPE;
    public static final Function<Integer, VoxelShape> EIGHT_AGE_SHAPES = i -> SHAPES8[i];
    public static final Function<Integer, VoxelShape> FOUR_AGE_SHAPES = i -> SHAPE4[i];

    private final int maxAge;
    private final Function<Integer, VoxelShape> shapeByAge;
    private final IntegerProperty ageProperty;

    private static IntegerProperty cachedAgeProperty;


    public CropBlock(int maxAge, Function<Integer, VoxelShape> shapeByAge, Properties properties) {
        super(properties.strength(cacheProperty(maxAge)));
        this.maxAge = maxAge;
        ageProperty = cachedAgeProperty;
        cachedAgeProperty = null;
        this.shapeByAge = shapeByAge;
    }

    private static float cacheProperty(int maxAge) {
        cachedAgeProperty = IntegerProperty.create("age", 0, maxAge);
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeByAge.apply(getAge(state));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return MoonFarmBlockEntity.canSurvive(this, level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(cachedAgeProperty);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !this.isMaxAge(state);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public void randomTick(BlockState cropState, ServerLevel level, BlockPos cropPos, RandomSource random) {
        if (shouldGrow(level, random, cropPos, cropState)) growByOne(level, cropState, cropPos);
    }

    public int getMaxAge() {
        return maxAge;
    }

    public int getAge(BlockState state) {
        return state.getValue(ageProperty);
    }

    public IntegerProperty getAgeProperty() {
        return ageProperty;
    }

    public boolean isMaxAge(BlockState state) {
        return getAge(state) == getMaxAge();
    }

    protected boolean shouldGrow(Level level, RandomSource random, BlockPos cropPos, BlockState cropState) {
        if(isMaxAge(cropState)) return false;
        if (level.getRawBrightness(cropPos, 0) < 9) return false;
        if (isMaxAge(cropState)) return false;
        float speed = MoonFarmBlockEntity.getSpeedForCrop(this, level, cropPos.below());
        return (speed > 0 && random.nextInt((int)(1 / speed) + 1) == 0);
    }

    protected void growByOne(Level level, BlockState state, BlockPos pos) {
        int age = getAge(state) + 1;
        level.setBlock(pos, state.setValue(ageProperty, age), 2);
        MoonFarmBlockEntity.takeCropMoisture(this, level, pos.below());
    }

}
