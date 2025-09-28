package net.smok.drifter.blocks.engine;

import earth.terrarium.adastra.common.blockentities.base.BasicContainer;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.FlyUtils;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EnginePanelBlockEntity extends ExtendedBlockEntity implements BasicContainer, WorldlyContainer,
        ShipBlock {

    public static final int SHAKING_TICKS = 40;
    public static final int BUCKET_INPUT = 0;
    public static final int BUCKET_OUTPUT = 1;
    public static final Pair<FluidHolder, Long> EMPTY_HOLDER = new Pair<>(FluidHolder.empty(), 1L);

    private final NonNullList<ItemStack> itemContainer;

    private final SavedDataSlot<Integer> fuelEfficiency = SavedDataSlot.intValue("fuelEfficiency", 1, 9999); // the fuel consumption tick rate
    private final SavedDataSlot<Integer> maxSpeed = SavedDataSlot.intValue("maxSpeed");
    private final SavedDataSlot<Integer> speed = SavedDataSlot.intValue("speed");

    // Not configurable
    private final BlockEntityPosition<TankBlockEntity> tank =
            new BlockEntityPosition<>("tank", DrifterBlocks.TANK_BLOCK_ENTITY.get());
    // Configurable. Save and load
    private final BlockEntityPosition<ShipControllerBlockEntity> controller =
            new BlockEntityPosition<>("controller", DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get());

    private final HashSet<BlockPos> nuzzles = new HashSet<>();
    private int shakingTicks;


    private final List<SavedDataSlot<?>> savedData = List.of(fuelEfficiency, maxSpeed, speed);

    public EnginePanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get(), pos, blockState);
        itemContainer = NonNullList.withSize(4, ItemStack.EMPTY);
        maxSpeed.setValue(80);
        fuelEfficiency.setValue(20);
        tank.setPos(getBlockPos().above());
    }

    @Override
    public NonNullList<ItemStack> items() {
        return itemContainer;
    }


    public Pair<FluidHolder, Long> getFluidHolder() {
        return tank.getBlock(level).map(tankBlockEntity ->
                        new Pair<>(
                                tankBlockEntity.getFluidContainer().getFirstFluid(),
                                tankBlockEntity.getFluidContainer().getTankCapacity(0))
                ).orElse(EMPTY_HOLDER);
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
        return Component.translatable("block.asteroid_drifter.engine_panel");
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
        controller.load(tag);
        nuzzles.clear();
        if (tag.contains("nuzzles", CompoundTag.TAG_LIST)) {
            for (Tag t : tag.getList("nuzzles", CompoundTag.TAG_COMPOUND)) {
                nuzzles.add(NbtUtils.readBlockPos((CompoundTag) t));
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, itemContainer);
        savedData.forEach(savedDataSlot -> savedDataSlot.save(tag));
        controller.save(tag);
        ListTag nuzzlesTag = new ListTag();
        for (BlockPos nuzzle : nuzzles) {
            nuzzlesTag.add(NbtUtils.writeBlockPos(nuzzle));
        }
        tag.put("nuzzles", nuzzlesTag);
    }

    public static boolean canPlace(int index, ItemStack item) {
        return true;
    }

    public static boolean canTake(int index, ItemStack item) {
        return true;
    }

    public void tick(ServerLevel lvl, long gameTime) {
        controller.executeIfPresentOrElse(level,
                controllerBlock ->
                {
                    if (!controllerBlock.isStand()) speedTick(controllerBlock.getRemainDistance());
                },
                pos ->
                {
                    if (controller.pos() != null) controller.setPos(null);
                    if (speed() > 0) speedTick(0);
                }
        );

        if (gameTime % 10L == 0) {
            tank.executeIfPresent(lvl, tankBlockEntity ->
            {
                FluidUtils.moveItemToContainer(
                        this, tankBlockEntity.getFluidContainer(), BUCKET_INPUT, BUCKET_OUTPUT, 0);
                tankBlockEntity.setChanged();
                setChanged();
            }
            );
            recountMaxSpeed();
        }

        if (gameTime % fuelEfficiency.get() == 0 && speed() > 0) {
            tank.getBlock(lvl).ifPresent(TankBlockEntity::decrease);
        }


        if (shakingTicks > 0) shakingTicks--;
    }

    public void speedTick(int remainDistance) {
        int maxSpeed = maxSpeed();
        int curSpeed = speed();

        if (shakingTicks > 0 || remainDistance < FlyUtils.brakingDistance(maxSpeed) || getFuel() <= 0) {
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

    private void recountMaxSpeed() {
        if (level == null || level.isClientSide) return;

        HashMap<EngineNozzleBlock, AtomicInteger> counter = new HashMap<>();
        for (BlockPos pos : new HashSet<>(nuzzles)) {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getBlock() instanceof EngineNozzleBlock nuzzle) {
                if (!counter.containsKey(nuzzle)) {
                    counter.put(nuzzle, new AtomicInteger(1));
                } else {
                    counter.get(nuzzle).incrementAndGet();
                }
            } else {
                nuzzles.remove(pos);
            }
        }

        double speed = counter.entrySet().stream().mapToDouble(pair -> Math.sqrt(pair.getValue().get()) * pair.getKey().getMaxSpeed()).sum();
        int last = maxSpeed();
        maxSpeed.setValue((int) speed);
        if (last != maxSpeed()) {
            shakeEngines();
            setChanged();
        }
    }

    private void shakeEngines() {
        shakingTicks += SHAKING_TICKS;
    }


    public long getFuel() {
        return tank.getBlock(level).map(tankBlockEntity -> tankBlockEntity.getFluidContainer().getFirstFluid().getFluidAmount()).orElse(0L);
    }

    public long getFuelCapacity() {
        return tank.getBlock(level).map(tankBlockEntity -> tankBlockEntity.getFluidContainer().getTankCapacity(0)).orElse(1L);
    }

    public Component getSpeed() {
        return Component.translatable("tooltip.asteroid_drifter.speed_container", String.format("%,d", FlyUtils.speedToKm(speed())));
    }

    public Component getMaxSpeed() {
        return Component.translatable("tooltip.asteroid_drifter.max_speed_container", String.format("%,d", FlyUtils.speedToKm(maxSpeed())));
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


    public int maxSpeed() {
        return maxSpeed.getValue();
    }

    public int speed() {
        return speed.getValue();
    }

    public boolean stand() {
        return controller.getBlock(level).map(ShipControllerBlockEntity::isStand).orElse(true);
    }

    @Override
    public boolean bind(BlockPos pos, ShipBlock other) {
        if (other instanceof ShipControllerBlockEntity) {
            controller.setPos(pos);
            return true;
        }
        if (other instanceof EngineNozzleBlock) {
            if (nuzzles.add(pos)) {
                recountMaxSpeed();
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        controller.setPos(null);
        nuzzles.clear();
        recountMaxSpeed();
        setChanged();
    }
}
