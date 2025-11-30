package net.smok.drifter.blocks.garden;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.utils.ExtraUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MoonFarmBlock extends BaseEntityBlock {

    public static final BooleanProperty FULL = BooleanProperty.create("full");

    private static final VoxelShape OUTER_SHAPE = Shapes.block();
    private static final VoxelShape DIRT_SHAPE = Shapes.join(OUTER_SHAPE, Block.box(2.0, 2.0, 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);


    public MoonFarmBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FULL, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DIRT_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hasSoil(state, level, pos) ? OUTER_SHAPE : DIRT_SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return OUTER_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoonFarmBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return blockEntityType == DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get()
                ? (level1, blockPos, blockState1, blockEntity) ->
                ((MoonFarmBlockEntity)blockEntity).tick() : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Optional<MoonFarmBlockEntity> blockEntity = level.getBlockEntity(pos, DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get());
        if (blockEntity.isEmpty()) return InteractionResult.PASS;

        MoonFarmBlockEntity block = blockEntity.get();
        ItemStack handItem = player.getItemInHand(hand);
        if (handItem.isEmpty()) return InteractionResult.PASS;

        // Hoe -> make farmland or remove nutrients
        if (handItem.is(ItemTags.HOES) && !block.soil().isEmpty()) {
            if (block.replaceDirtToFarmland() || block.digNutrients()) {
                handItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }

        // Shovel -> remove soil
        if (handItem.is(ItemTags.SHOVELS) && !block.soil().isEmpty()) {
            if (!block.soil().isEmpty()) {
                Vec3 itemPosition = Vec3.atLowerCornerWithOffset(pos, 0.5, 1.01, 0.5).offsetRandom(level.random, 0.7F);
                ItemEntity itemEntity = new ItemEntity(level, itemPosition.x(), itemPosition.y(), itemPosition.z(), block.soil().is(Items.FARMLAND) ? new ItemStack(Items.DIRT) : block.soil());
                level.addFreshEntity(itemEntity);
                block.digAll();
                level.setBlockAndUpdate(pos, state.setValue(FULL, false));

                handItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }

        // All items -> put soil
        if (block.putSoil(handItem, level.isClientSide, !player.isCreative())) {
            SoundEvent placeSound = handItem.getItem() instanceof BlockItem blockItem ?
                    blockItem.getBlock().getSoundType(blockItem.getBlock().defaultBlockState()).getPlaceSound() :
                    SoundEvents.COMPOSTER_FILL;

            BlockState blockState = pushEntitiesUp(state, state.setValue(FULL, true), level, pos);
            level.setBlockAndUpdate(pos, blockState);
            level.playSound(player, pos, placeSound, SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            return InteractionResult.SUCCESS;
        }


        // All items -> put soil or nutrients
        if (block.putNutrients(handItem, level.isClientSide, !player.isCreative())) {
            SoundEvent placeSound = handItem.getItem() instanceof BlockItem blockItem ?
                    blockItem.getBlock().getSoundType(blockItem.getBlock().defaultBlockState()).getPlaceSound() :
                    SoundEvents.COMPOSTER_FILL;

            level.playSound(player, pos, placeSound, SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            return InteractionResult.SUCCESS;
        }

        if (block.canPutNutrientsFluid(handItem)) return ExtraUtils.putFluidBucket(level, pos, player);
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean hasSoil(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FULL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FULL);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            level.getBlockEntity(pos, DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get()).ifPresent(moonFarmBlockEntity -> {
                Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, moonFarmBlockEntity.soil()));
                //level.updateNeighbourForOutputSignal(pos, this);
            });

            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
