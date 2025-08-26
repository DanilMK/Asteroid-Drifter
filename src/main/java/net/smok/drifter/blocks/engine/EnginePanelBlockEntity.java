package net.smok.drifter.blocks.engine;

import earth.terrarium.adastra.common.blockentities.base.BasicContainer;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.adastra.common.utils.ItemUtils;
import earth.terrarium.botarium.common.fluid.FluidApi;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.*;
import earth.terrarium.botarium.common.fluid.impl.InsertOnlyFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import earth.terrarium.botarium.common.menu.ExtraDataMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.Debug;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.FlyUtils;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.List;

public class EnginePanelBlockEntity extends BlockEntity implements BasicContainer, WorldlyContainer,
        ExtraDataMenuProvider, ShipBlock {

    public static final int TANK_0 = 0;
    public static final int SECOND_UPGRADE = 1;
    public static final int BUCKET_INPUT = 2;
    public static final int BUCKET_OUTPUT = 3;

    private final NonNullList<ItemStack> itemContainer;

    private final SavedDataSlot<Integer> fuelEfficiency = SavedDataSlot.intValue("fuelEfficiency", 1, 9999); // the fuel consumption tick rate
    private final SavedDataSlot<Integer> maxSpeed = SavedDataSlot.intValue("maxSpeed");
    private final SavedDataSlot<Integer> speed = SavedDataSlot.intValue("speed");



    private int changeCount;

    private final List<SavedDataSlot<?>> savedData = List.of(fuelEfficiency, maxSpeed, speed);

    public EnginePanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get(), pos, blockState);
        itemContainer = NonNullList.withSize(4, ItemStack.EMPTY);
        maxSpeed.setValue(80);
        fuelEfficiency.setValue(20);
    }

    @Override
    public NonNullList<ItemStack> items() {
        return itemContainer;
    }


    public Pair<FluidHolder, Long> getFluidHolder() {
        ItemStack item = getItem(TANK_0);
        if (FluidUtils.hasFluid(item)) return new Pair<>(FluidUtils.getTank(item), FluidUtils.getTankCapacity(item));
        return new Pair<>(FluidHolder.empty(), 1L);
    }

    private @NotNull WrappedBlockFluidContainer createContainer() {
        return new WrappedBlockFluidContainer(this,
                new InsertOnlyFluidContainer(value -> FluidConstants.fromMillibuckets(MachineConfig.deshTierFluidCapacity), 1,
                        (integer, fluidHolder) -> true)
        );
    }

    @Override
    public void writeExtraData(ServerPlayer serverPlayer, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(getBlockPos());
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return switch (side) {
            case DOWN -> new int[] {3};
            case UP, NORTH, EAST, SOUTH, WEST -> new int[] {2};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return canPlace(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return canTake(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("block.asteroid_drifter.engine_panel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EngineMenu(i, inventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, itemContainer);
        savedData.forEach(savedDataSlot -> savedDataSlot.load(tag));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, itemContainer);
        savedData.forEach(savedDataSlot -> savedDataSlot.save(tag));
    }

    public static boolean canPlace(int index, ItemStack item) {
        return true;
    }

    public static boolean canTake(int index, ItemStack item) {
        return true;
    }

    public void tick(ServerLevel lvl, long gameTime, BlockState blockState, BlockPos blockPos) {
        if (gameTime % 10L == 0) {

            ItemStack item = getItem(TANK_0);
            if (!item.isEmpty() && item.getItem() instanceof BotariumFluidItem<?>) {

                if (moveFluid(this, BUCKET_INPUT, BUCKET_OUTPUT, TANK_0)) {
                    setChanged();
                }

            }
            //FluidUtils.moveItemToContainer(this, getFluidContainer(), BUCKET_INPUT, BUCKET_OUTPUT, 0);
        }
    }

    private static boolean moveFluid(Container container, int bucketInSlot, int bucketOutSlot, int containerSlot) {
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
            container.setItem(BUCKET_OUTPUT, outResult);

        } else if (ItemUtils.canAddItem(outResult, bucketOut)) {
            bucketIn.shrink(1);
            bucketOut.grow(1);
        }
        container.setItem(containerSlot, filledStack);

        return true;

    }

    public void fuelTick(long gameTime) {
        if (gameTime % fuelEfficiency.get() == 0) {
            if (getFuel() > 0) {

                ItemStack item = getItem(TANK_0);
                ItemStack filledStack = FluidUtils.getEmptyStack(new ItemStackHolder(item), FluidUtils.getTank(item).copyWithAmount(81));
                setItem(TANK_0, filledStack);


                setChanged();
            }
        }
    }

    public void speedTick(int remainDistance) {
        int maxSpeed = maxSpeed();
        int curSpeed = speed();

        if (remainDistance < FlyUtils.brakingDistance(maxSpeed) || getFuel() <= 0) {
            speed.setValue(Math.max(curSpeed - FlyUtils.BRAKING, 0));
            setChanged();
        } else if (curSpeed < maxSpeed) {
            speed.setValue(Math.min(curSpeed + FlyUtils.ACCELERATION, maxSpeed));
            setChanged();
        }
    }

    public void resetSpeed() {
        speed.setValue(0);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        changeCount++;
    }

    public long getFuel() {
        return FluidUtils.getTank(getItem(TANK_0)).getFluidAmount();
    }

    public long getFuelCapacity() {
        return FluidUtils.getTankCapacity(getItem(TANK_0));
    }


    public Component getAmount() {
        return Component.literal(FluidConstants.toMillibuckets(getFuel())
                + " / " + FluidConstants.toMillibuckets(getFuelCapacity())).withStyle(ChatFormatting.WHITE);
    }

    public Component getRequired(int distance) {
        float v = FlyUtils.fuelCost(maxSpeed(), distance, getFuelEfficiency());
        long fuel = FluidConstants.toMillibuckets(getFuel());
        return Component.translatable("tooltip.asteroid_drifter.fuel_required", fuel, String.format("%.1f", v)).withStyle(v > fuel ? ChatFormatting.RED : ChatFormatting.GRAY);
    }

    public boolean canLaunch(int distance) {
        return FluidConstants.toMillibuckets(getFuel()) > FlyUtils.fuelCost(maxSpeed(), distance, getFuelEfficiency());
    }

    public Component fuelConsumption() {
        return Component.translatable("tooltip.asteroid_drifter.fuel_container", String.format("%.1f", getFuelEfficiency() / 20f));
    }

    public int getFuelEfficiency() {
        return fuelEfficiency.get();
    }

    public int getChangeCount() {
        return changeCount;
    }

    public int maxSpeed() {
        return maxSpeed.getValue();
    }

    public int speed() {
        return speed.getValue();
    }

    public List<SavedDataSlot<?>> getSavedData() {
        return savedData;
    }
}
