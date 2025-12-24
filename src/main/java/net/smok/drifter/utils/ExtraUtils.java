package net.smok.drifter.utils;

import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.adastra.common.utils.ItemUtils;
import earth.terrarium.botarium.common.fluid.FluidApi;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidBlock;
import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.impl.InsertOnlyFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import earth.terrarium.botarium.util.Updatable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.blocks.engine.TankBlockEntity;
import net.smok.drifter.registries.DrifterBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class ExtraUtils {
    public static @NotNull InteractionResult handleBucketUseOnTank(Level level, BlockPos pos, Player player) {
        ItemStack bucketIn = player.getMainHandItem();
        Optional<TankBlockEntity> blockEntity = level.getBlockEntity(pos, DrifterBlocks.TANK_BLOCK_ENTITY.get());

        if (blockEntity.isEmpty()) return InteractionResult.PASS;
        TankBlockEntity tankBlock = blockEntity.get();

        ItemStackHolder bucketHolder = new ItemStackHolder(bucketIn.copyWithCount(1));
        FluidContainer bucketContainer = FluidContainer.of(bucketHolder);
        FluidContainer tankContainer = tankBlock.getFluidContainer();

        if (bucketContainer == null || tankContainer == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        FluidHolder bucketAmount = bucketContainer.getFluids().get(0).copyHolder();
        FluidHolder tankAmount = tankContainer.getFluids().get(0).copyHolder();
        if (bucketAmount.isEmpty() && tankAmount.isEmpty() || !tankBlock.canBeAccessed(0, bucketAmount))
            return InteractionResult.SUCCESS;

        boolean tankToBucket = bucketAmount.isEmpty();
        FluidHolder amount = tankToBucket ? tankAmount : bucketAmount;

        FluidContainer from = tankToBucket ? tankContainer : bucketContainer;
        FluidContainer to = tankToBucket ? bucketContainer : tankContainer;


        if (FluidApi.moveFluid(from, to, amount, true) == 0L) return InteractionResult.SUCCESS;

        FluidApi.moveFluid(from, to, amount, false);
        tankBlock.setChanged();

        giveOrCreateItem(level, pos, player, bucketIn, bucketHolder.getStack());

        return InteractionResult.SUCCESS;
    }


    public static <T extends FluidContainer & Updatable<BlockEntity>> @NotNull InteractionResult
    putFluidBucket(Level level, BlockPos pos, Player player) {

        ItemStack bucketIn = player.getMainHandItem();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BotariumFluidBlock<?> fluidBlock)) return InteractionResult.PASS;
        T tankContainer = (T) fluidBlock.getFluidContainer();


        ItemStackHolder bucketHolder = new ItemStackHolder(bucketIn.copyWithCount(1));
        FluidContainer bucketContainer = FluidContainer.of(bucketHolder);

        if (bucketContainer == null || tankContainer == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        FluidHolder bucketAmount = bucketContainer.getFluids().get(0).copyHolder();
        if (bucketAmount.isEmpty())
            return InteractionResult.SUCCESS;

        if (moveFluid(bucketContainer, tankContainer, bucketAmount, tankContainer, blockEntity))
            giveOrCreateItem(level, pos, player, bucketIn, bucketHolder.getStack());

        return InteractionResult.SUCCESS;
    }

    private static <T extends FluidContainer & Updatable<BlockEntity>> boolean
    moveFluid(FluidContainer from, FluidContainer to, FluidHolder amount, T tankContainer, BlockEntity blockEntity) {
        if (FluidApi.moveFluid(from, to, amount, true) == 0L) return false;
        FluidApi.moveFluid(from, to, amount, false);
        tankContainer.update(blockEntity);
        return true;
    }


    public static void giveOrCreateItem(Level level, BlockPos pos, Player player, ItemStack itemToShrink, ItemStack itemToGrow) {
        if (!player.isCreative()) {
            itemToShrink.shrink(1);
            boolean added = player.getInventory().add(itemToGrow);

            if (added) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                        ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                player.containerMenu.broadcastChanges();
            } else {
                ItemEntity itemEntity = player.drop(itemToGrow, false);
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setTarget(player.getUUID());
                }
            }
        }
    }

    public static boolean moveFluidFromItemToItem(Container container, int bucketInSlot, int bucketOutSlot, int containerSlot) {
        ItemStack bucketOut = container.getItem(bucketOutSlot);
        ItemStack bucketIn = container.getItem(bucketInSlot);
        ItemStack result = container.getItem(containerSlot);


        if (bucketIn.isEmpty() || result.isEmpty()) return false;

        ItemStackHolder inStackHolder = new ItemStackHolder(bucketIn.copyWithCount(1));
        ItemStackHolder resultStackHolder = new ItemStackHolder(result.copyWithCount(1));

        FluidContainer inFluidContainer = FluidContainer.of(inStackHolder);
        FluidContainer resultFluidContainer = FluidContainer.of(resultStackHolder);

        if (inFluidContainer == null || resultFluidContainer == null) return false;
        FluidHolder amount = inFluidContainer.getFluids().get(0).copyHolder();
        ItemStack filledStack = FluidUtils.getFilledStack(resultStackHolder, amount);
        if (amount.isEmpty()) return false;

        ItemStack outResult;
        if (!bucketOut.isEmpty()) {
            outResult = FluidUtils.getEmptyStack(inStackHolder, amount);
            if (!ItemUtils.canAddItem(outResult, bucketOut)) return false;
        }

        if (FluidApi.moveFluid(inFluidContainer, resultFluidContainer, amount, true) == 0L) return false;
        FluidApi.moveFluid(inFluidContainer, resultFluidContainer, amount, false);


        outResult = inStackHolder.getStack();
        if (bucketOut.isEmpty()) {
            bucketIn.shrink(1);
            container.setItem(EnginePanelBlockEntity.BUCKET_OUTPUT, outResult);

        } else if (ItemUtils.canAddItem(outResult, bucketOut)) {
            bucketIn.shrink(1);
            bucketOut.grow(1);
        }
        container.setItem(containerSlot, filledStack);

        return true;

    }

    public static LevelChunk @NotNull [] chunks3x3(@NotNull Level level, @NotNull BlockPos blockPos) {
        LevelChunk chunk = level.getChunkAt(blockPos);
        ChunkPos chunkPos = chunk.getPos();

        return new LevelChunk[] {
                chunk,
                level.getChunk(chunkPos.x - 1, chunkPos.z - 1),
                level.getChunk(chunkPos.x - 1, chunkPos.z),
                level.getChunk(chunkPos.x - 1, chunkPos.z + 1),
                level.getChunk(chunkPos.x, chunkPos.z - 1),
                level.getChunk(chunkPos.x, chunkPos.z + 1),
                level.getChunk(chunkPos.x + 1, chunkPos.z - 1),
                level.getChunk(chunkPos.x + 1, chunkPos.z),
                level.getChunk(chunkPos.x + 1, chunkPos.z + 1)
        };
    }

    public static LevelChunk @NotNull [] chunks5x5(@NotNull Level level, @NotNull BlockPos blockPos) {
        LevelChunk chunk = level.getChunkAt(blockPos);
        ChunkPos chunkPos = chunk.getPos();

        return new LevelChunk[] {
                chunk,
                level.getChunk(chunkPos.x - 1, chunkPos.z - 1),
                level.getChunk(chunkPos.x - 1, chunkPos.z),
                level.getChunk(chunkPos.x - 1, chunkPos.z + 1),
                level.getChunk(chunkPos.x, chunkPos.z - 1),
                level.getChunk(chunkPos.x, chunkPos.z + 1),
                level.getChunk(chunkPos.x + 1, chunkPos.z - 1),
                level.getChunk(chunkPos.x + 1, chunkPos.z),
                level.getChunk(chunkPos.x + 1, chunkPos.z + 1),
                level.getChunk(chunkPos.x - 2, chunkPos.z - 2),
                level.getChunk(chunkPos.x - 2, chunkPos.z - 1),
                level.getChunk(chunkPos.x - 2, chunkPos.z),
                level.getChunk(chunkPos.x - 2, chunkPos.z + 1),
                level.getChunk(chunkPos.x - 2, chunkPos.z + 2),
                level.getChunk(chunkPos.x - 1, chunkPos.z - 2),
                level.getChunk(chunkPos.x - 1, chunkPos.z + 2),
                level.getChunk(chunkPos.x, chunkPos.z - 2),
                level.getChunk(chunkPos.x, chunkPos.z + 2),
                level.getChunk(chunkPos.x + 1, chunkPos.z - 2),
                level.getChunk(chunkPos.x + 1, chunkPos.z + 2),
                level.getChunk(chunkPos.x + 2, chunkPos.z - 2),
                level.getChunk(chunkPos.x + 2, chunkPos.z - 1),
                level.getChunk(chunkPos.x + 2, chunkPos.z),
                level.getChunk(chunkPos.x + 2, chunkPos.z + 1),
                level.getChunk(chunkPos.x + 2, chunkPos.z + 2)
        };
    }

    public static float @NotNull [] colorIntToFloats(int color) {
        float r = (float) FastColor.ARGB32.red(color) / 255.0F;
        float g = (float) FastColor.ARGB32.green(color) / 255.0F;
        float b = (float) FastColor.ARGB32.blue(color) / 255.0F;
        float a = (float) FastColor.ARGB32.alpha(color) / 255.0F;

        return new float[]{r, g, b, a};
    }


    public <C extends Container, T extends Recipe<C> & RecipeWithFluidInput> WrappedBlockFluidContainer
    createInFluidContainer(Level level, BlockEntity entity, long capacity, int tanks, RecipeType<T> type) {

        return new WrappedBlockFluidContainer(entity, new InsertOnlyFluidContainer(value -> capacity, tanks,
                (tank, holder) -> level.getRecipeManager().getAllRecipesFor(type).stream().anyMatch(t -> t.fluidMatch(holder))));

    }

    public interface RecipeWithFluidInput {
        boolean fluidMatch(FluidHolder holder);
    }

    public static boolean putItemToContainer(@NotNull Container container, @NotNull ItemStack from) {
        int initAmount = from.getCount();
        for (int slot = 0; slot < container.getContainerSize() && from.getCount() > 0; slot++) {
            ItemStack to = container.getItem(slot);
            if (to.isEmpty() || (ItemStack.isSameItemSameTags(to, from) &&
                    to.getCount() < to.getMaxStackSize() && to.getCount() < container.getMaxStackSize())) {

                if (to.isEmpty()) {
                    container.setItem(slot, from.copyWithCount(Math.min(from.getCount(), container.getMaxStackSize())));
                    from.shrink(container.getItem(slot).getCount());
                    continue;
                }

                int grow = Math.min(Math.min(to.getMaxStackSize() - to.getCount(), container.getMaxStackSize() - to.getCount()),  from.getCount());
                if (grow == 0) continue;
                to.grow(grow);
                from.shrink(grow);
            }
        }

        boolean b = initAmount != from.getCount();
        if (b) container.setChanged();
        return b;
    }
}
