package net.smok.drifter.blocks.garden;

import earth.terrarium.adastra.common.blockentities.base.BasicContainer;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidBlock;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.data.recipies.MoonFarmRecipe;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoonFarmBlockEntity extends BlockEntity implements BasicContainer, BotariumFluidBlock<WrappedBlockFluidContainer> {

    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private final WrappedBlockFluidContainer fluidContainer =
            new WrappedBlockFluidContainer(this, new SimpleFluidContainer(value ->
                    FluidConstants.fromMillibuckets(MachineConfig.steelTierFluidCapacity), 5,
                    (integer, holder) -> true));

    private @Nullable MoonFarmRecipe cachedRecipe = null;
    private List<MoonFarmRecipe> cachedRecipes;


    public MoonFarmBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tick() {
    }

    private void cacheRecipe() {
        if (level != null && level.getGameTime() % 20L == 0) {
            BlockState blockState = level.getBlockState(getBlockPos().above());
            Block block = blockState.getBlock();
            if (blockState.isAir()) {
                cachedRecipe = null;
            } else if (cachedRecipe == null || block.equals(cachedRecipe.cropBlock())) {
                findRecipe(block).ifPresent(recipe -> cachedRecipe = recipe);
            }
        }
    }

    /**
     * Return speed by recipe, between 0 and 1. Return -1 if no recipe;
     */
    public float getSpeed(Block cropBlock) {
        return findRecipe(cropBlock).map(recipe -> recipe.sumSpeed(this)).orElse(0f);
    }

    /**
     * Return if any recipe has this block and block feet soil and oxygen
     */
    public boolean canSurvive(Block cropBlock) {
        return findRecipe(cropBlock).map(recipe -> recipe.soil().test(soil())).orElse(false);
    }

    /**
     * Take nutrients for recipe for CropBlock
     */
    public void takeNutrients(Block cropBlock) {
        findRecipe(cropBlock).ifPresent(recipe -> {
            for (MoonFarmRecipe.Nutrient nutrient : recipe.nutrients()) takeForNutrient(nutrient);
            setChanged();
        });
    }

    private void takeForNutrient(MoonFarmRecipe.Nutrient nutrient) {
        if (!nutrient.itemIngredient().isEmpty()) {
            for (ItemStack itemStack : nutrients()) {
                for (ItemStack ingredient : nutrient.itemIngredient().getItems()) {
                    if (!itemStack.isEmpty() && itemStack.is(ingredient.getItem())) {
                        if (level.random.nextFloat() < nutrient.takeProbability())
                            itemStack.shrink(ingredient.getCount());
                        return;
                    }
                }
            }
        }
        if (nutrient.fluidIngredient().getFluidAmount() > 0) {
            for (FluidHolder fluid : fluidContainer.getFluids()) {
                if (nutrient.fluidIngredient().test(fluid)) {
                    if (level.random.nextFloat() < nutrient.takeProbability())
                        fluid.setAmount(Math.max(fluid.getFluidAmount() - nutrient.fluidIngredient().getFluidAmount(), 0));
                    return;
                }
            }
        }
    }

    /**
     * Clear all items and liquids
     *
     * @return Whether there was soil
     */
    public boolean digAll() {
        boolean result = !soil().isEmpty();
        items.clear();
        fluidContainer.clearContent();
        return result;
    }

    public boolean replaceDirtToFarmland() {
        if (soil().is(Items.DIRT)) {
            setItem(0, new ItemStack(Items.FARMLAND));
            setChanged();
            return true;
        }
        return false;
    }

    /**
     * Clear only moisture items and liquids
     *
     * @return Whether there were nutrients
     */
    public boolean digNutrients() {
        boolean result = false;
        for (int i = 1; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) result = true;
            items.set(i, ItemStack.EMPTY);
        }
        for (FluidHolder fluid : fluidContainer.getFluids()) {
            if (!fluid.isEmpty()) result = true;
            fluid.setAmount(0);
        }
        return result;
    }


    /**
     * Put item to soil slot.
     * @param item Placed item.
     * @param simulate Only return result of method, doesn't put item.
     * @param shrink Should take item from filled. (F.e. in survival mode)
     * @return Returns whether an item could be placed to soil slot.
     */
    public boolean putSoil(ItemStack item, boolean simulate, boolean shrink) {
        return getItem(0).isEmpty() && canPutSoil(item) && putNew(item, simulate, 0, shrink);
    }

    /**
     * Put item to nutrients slot.
     * @param item Placed item.
     * @param simulate Only return result of method, doesn't put item.
     * @param shrink Should take item from filled. (F.e. in survival mode)
     * @return Returns whether an item could be placed to nutrients slot.
     */
    public boolean putNutrients(ItemStack item, boolean simulate, boolean shrink) {

        // first put all same items
        for (int slot = 1; slot < items.size(); slot++) if (putSame(item, simulate, getItem(slot), shrink)) return true;
        if (!canPutNutrients(item)) return false;
        for (int slot = 1; slot < items.size(); slot++) if (putNew(item, simulate, slot, shrink)) return true;

        return false;
    }

    private boolean putNew(ItemStack item, boolean simulate, int slot, boolean shrink) {
        if (!items.get(slot).isEmpty()) return false;
        if (simulate) return true;
        items.set(slot, item.copyWithCount(1));
        if (shrink) item.shrink(1);
        setChanged();
        return true;
    }

    private boolean putSame(ItemStack item, boolean simulate, ItemStack innerItem, boolean shrink) {
        if (innerItem.isEmpty()) return false;
        // cannot add more 16 items
        if (!ItemStack.isSameItemSameTags(item, innerItem) || innerItem.getCount() >= innerItem.getMaxStackSize() / 4) return false;
        if (simulate) return true;
        innerItem.grow(1);
        if (shrink) item.shrink(1);
        setChanged();
        return true;
    }

    private boolean canPutSoil(ItemStack item) {
        return level.getRecipeManager().getAllRecipesFor(Values.MOON_FARMLAND_TYPE.get())
                .stream().anyMatch(recipe -> recipe.soil().test(item));
    }

    private boolean canPutNutrients(ItemStack item) {
        return level.getRecipeManager().getAllRecipesFor(Values.MOON_FARMLAND_TYPE.get()).stream()
                .anyMatch(recipe -> recipe.nutrients().stream().anyMatch(requirement ->
                        requirement.itemIngredient().test(item)
                ));
    }

    public boolean canPutNutrientsFluid(ItemStack item) {
        return FluidUtils.hasFluid(item) && level.getRecipeManager().getAllRecipesFor(Values.MOON_FARMLAND_TYPE.get()).stream()
                .anyMatch(recipe -> recipe.nutrients().stream().anyMatch(requirement ->
                        requirement.fluidIngredient().test(FluidUtils.getTank(item))
                ));
    }

    public @NotNull Optional<MoonFarmRecipe> findRecipe(Block cropBlock) {
        if (cachedRecipe != null && cachedRecipe.cropBlock().equals(cropBlock)) return Optional.of(cachedRecipe);
        if (level == null) return Optional.empty();
        Optional<MoonFarmRecipe> first = level.getRecipeManager().getAllRecipesFor(Values.MOON_FARMLAND_TYPE.get()).stream()
                .filter(recipe -> recipe.matches(this, level) && recipe.cropBlock().equals(cropBlock)).findFirst();
        first.ifPresent(recipe -> cachedRecipe = recipe);
        return first;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void takeCropMoisture(Block cropBlock, BlockGetter level, BlockPos farmlandPos) {
        level.getBlockEntity(farmlandPos, DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get()).ifPresent(farm -> farm.takeNutrients(cropBlock));
    }

    public static float getSpeedForCrop(Block cropBlock, BlockGetter level, BlockPos farmlandPos) {
        return level.getBlockEntity(farmlandPos, DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get())
                .map(farmland -> farmland.getSpeed(cropBlock)).orElse(-1f);
    }

    public static boolean canSurvive(Block cropBlock, BlockGetter level, BlockPos farmlandPos) {
        return level.getBlockEntity(farmlandPos, DrifterBlocks.MOON_FARM_BLOCK_ENTITY.get())
                .map(farmland -> farmland.canSurvive(cropBlock)).orElse(false);
    }

    @Override
    public NonNullList<ItemStack> items() {
        return items;
    }

    @Override
    public WrappedBlockFluidContainer getFluidContainer() {
        return fluidContainer;
    }

    public @NotNull ItemStack soil() {
        return items.get(0);
    }

    public @NotNull List<ItemStack> nutrients() {
        List<ItemStack> list = new ArrayList<>(items);
        list.remove(0);
        return list;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, items);
        fluidContainer.serialize(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        fluidContainer.deserialize(tag);
    }
}
