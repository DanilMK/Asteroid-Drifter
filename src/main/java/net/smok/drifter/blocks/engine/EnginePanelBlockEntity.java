package net.smok.drifter.blocks.engine;

import earth.terrarium.adastra.common.blockentities.base.BasicContainer;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.botarium.common.fluid.FluidApi;
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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.menus.EngineMenu;
import net.smok.drifter.recipies.FuelRecipe;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.registries.DrifterRecipes;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.SavedDataSlot;
import net.smok.drifter.ShipConfig;
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

    private final SavedDataSlot<Float> maxSpeed = SavedDataSlot.floatValue("maxSpeed");
    private final SavedDataSlot<Float> maxLimitedSpeed = SavedDataSlot.floatValue("maxLimitedSpeed");
    private final SavedDataSlot<Float> speed = SavedDataSlot.floatValue("speed");
    private final SavedDataSlot<Float> lastDistanceFuelDecrees = SavedDataSlot.floatValue("lastDistanceFuelDecrees");

    // Not configurable
    private final BlockEntityPosition<TankBlockEntity> tank =
            new BlockEntityPosition<>("tank", DrifterBlocks.TANK_BLOCK_ENTITY.get());
    // Configurable. Save and load
    private final BlockEntityPosition<ShipControllerBlockEntity> controller =
            new BlockEntityPosition<>("controller", DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get());

    private final HashSet<BlockPos> nuzzles = new HashSet<>();
    private final RecipeManager.CachedCheck<EnginePanelBlockEntity, FuelRecipe> quickCheck =
            RecipeManager.createCheck(DrifterRecipes.FUEL_TYPE.get());
    private int shakingTicks;
    @Nullable
    private FuelRecipe recipe;


    private final List<SavedDataSlot<?>> savedData = List.of(maxSpeed, speed, lastDistanceFuelDecrees, maxLimitedSpeed);

    public EnginePanelBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get(), pos, blockState);
        itemContainer = NonNullList.withSize(2, ItemStack.EMPTY);
        maxSpeed.setValue(ShipConfig.startSpeed());
        tank.setPos(getBlockPos().above());
    }

    @Override
    public NonNullList<ItemStack> items() {
        return itemContainer;
    }


    /**
     * Fuel Tank with capacity
     */
    public Pair<FluidHolder, Long> getFluidHolder() {
        return tank.getBlock(level).map(tankBlockEntity ->
                        new Pair<>(
                                tankBlockEntity.getFluidContainer().getFirstFluid(),
                                tankBlockEntity.getFluidContainer().getTankCapacity(0))
                ).orElse(EMPTY_HOLDER);
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return new int[BUCKET_OUTPUT];
        return new int[] {BUCKET_INPUT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return canPlace(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return canTake(index);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public @NotNull Component getDisplayName() {
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
        return index == BUCKET_INPUT && FluidApi.isFluidContainingItem(item);
    }

    public static boolean canTake(int index) {
        return index == BUCKET_OUTPUT;
    }

    public void tick(ServerLevel lvl, long gameTime) {
        if (speed() > 0) {
            Optional<ShipControllerBlockEntity> block = controller.getBlock(level);
            if (block.isEmpty() || block.get().isStand()) speedTick(0);
        }

        tank.getBlock(level).ifPresent(t -> {
            if (t.getFluidContainer().isEmpty()) recipe = null;
            quickCheckRecipe();
        });

        // handle fuel load to tank
        if (gameTime % 20L == 3){
            if (!getItem(BUCKET_INPUT).isEmpty()) {
                tank.executeIfPresent(lvl, tankBlockEntity ->
                        {
                            FluidUtils.moveItemToContainer(
                                    this, tankBlockEntity.getFluidContainer(), BUCKET_INPUT, BUCKET_OUTPUT, 0);
                            tankBlockEntity.setChanged();
                            setChanged();
                        }
                );
            }
            recountMaxSpeed();
            setChanged();
        }

        fuelTick(lvl, gameTime);
        if (shakingTicks > 0) shakingTicks--;
    }

    private void fuelTick(ServerLevel lvl, long gameTime) {
        if (gameTime % 20L == 0 && speed() > 0 && recipe != null) {
            Float remainDistance = controller.getBlock(level).map(ShipControllerBlockEntity::getRemainDistance).orElse(0f);
            float mbDecrease = recipe.kmToMb(lastDistanceFuelDecrees.getValue() - remainDistance);

            if (mbDecrease < 0) { // If the ship has started flying, the remaining distance will be greater than the last saved one.
                lastDistanceFuelDecrees.setValue(remainDistance);
                setChanged();
            } else if (mbDecrease > 1) {

                // Subtract the long part of value, extract from tank, save float remainder
                tank.getBlock(lvl).ifPresent(t -> t.getFluidContainer()
                        .internalExtract(t.getFluidContainer().getFirstFluid()
                                .copyWithAmount(FluidConstants.fromMillibuckets((long) mbDecrease)), false));
                lastDistanceFuelDecrees.setValue(remainDistance + recipe.mbToKm(mbDecrease % 1f));
                setChanged();

                if (lvl.random.nextFloat() < recipe.slagChance() * mbDecrease) {

                    BlockPos slugNuzzle = new ArrayList<>(nuzzles).get(lvl.random.nextInt(nuzzles.size()));
                    lvl.getBlockState(slugNuzzle).getOptionalValue(EngineNozzleBlock.FACING).ifPresent(direction -> {
                        BlockPos pos = slugNuzzle.relative(direction);
                        if (lvl.getBlockState(pos).isAir()) {
                            lvl.setBlock(pos, recipe.slugBlock().getState(lvl.random, pos), 3);
                        } else {
                            Vec3 center = pos.getCenter();
                            lvl.explode(null, center.x, center.y, center.z, 1, Level.ExplosionInteraction.BLOCK);
                        }
                        recountMaxSpeed();
                    });
                }
                if (lvl.random.nextFloat() < 0.01f * mbDecrease) {

                    BlockPos slugNuzzle = new ArrayList<>(nuzzles).get(lvl.random.nextInt(nuzzles.size()));
                    lvl.getBlockState(slugNuzzle).getOptionalValue(EngineNozzleBlock.FACING).ifPresent(direction -> {
                        BlockPos pos = slugNuzzle.relative(direction);
                        if (!lvl.getBlockState(pos).isAir()) {
                            Vec3 center = pos.getCenter();
                            lvl.explode(null, center.x, center.y, center.z, 1, Level.ExplosionInteraction.BLOCK);
                            recountMaxSpeed();
                        }
                    });
                }
            }
        }
    }


    /**
     * Accelerate speed only from ship controller or brake speed.
     * @param remainDistance if >0 - accelerate speed to max, else brake speed to 0
     */
    public void speedTick(float remainDistance) {
        float maxSpeed = maxLimitedSpeed();
        float curSpeed = speed();

        if (shakingTicks > 0 || curSpeed > maxSpeed || remainDistance < maxSpeed * ShipConfig.brakingTime / 3 || getFuel() <= 0) {
            speed.setValue(ShipConfig.brakeTick(curSpeed, maxSpeed));
            setChanged();
        } else if (curSpeed < maxSpeed) {
            speed.setValue(ShipConfig.accelerateTick(curSpeed, maxSpeed));
            setChanged();
        }
    }

    public void resetSpeed() {
        speed.setValue(0f);
        setChanged();
    }

    private void recountMaxSpeed() {
        if (level == null || level.isClientSide) return;

        Direction direction = controller.getBlock(level).map(c -> c.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING)).orElse(getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING));

        HashMap<EngineNozzleBlock, AtomicInteger> counter = new HashMap<>();
        HashMap<EngineNozzleBlock, AtomicInteger> counterLimited = new HashMap<>();
        boolean stand = stand();
        for (BlockPos pos : new HashSet<>(nuzzles)) {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getBlock() instanceof EngineNozzleBlock nuzzle &&
                    blockState.getValue(EngineNozzleBlock.FACING) == direction) {

                level.setBlock(pos, blockState.setValue(EngineNozzleBlock.LIT, !stand), 3);

                if (!counter.containsKey(nuzzle)) {
                    counter.put(nuzzle, new AtomicInteger(1));
                } else {
                    counter.get(nuzzle).incrementAndGet();
                }
                if (level.getBlockState(pos.relative(direction)).isAir()) {
                    if (!counterLimited.containsKey(nuzzle)) {
                        counterLimited.put(nuzzle, new AtomicInteger(1));
                    } else {
                        counterLimited.get(nuzzle).incrementAndGet();
                    }
                }
            } else {
                nuzzles.remove(pos);
            }
        }

        double speed = counter.entrySet().stream().mapToDouble(pair -> Math.sqrt(pair.getValue().get()) * pair.getKey().getMaxSpeed()).sum();
        double limitedSpeed = counterLimited.entrySet().stream().mapToDouble(pair -> Math.sqrt(pair.getValue().get()) * pair.getKey().getMaxSpeed()).sum();
        float last = maxSpeed();
        maxSpeed.setValue((float) speed);
        maxLimitedSpeed.setValue((float) limitedSpeed);
        if (last != maxSpeed()) {
            shakeEngines();
            setChanged();
        }
    }

    private void shakeEngines() {
        shakingTicks = SHAKING_TICKS;
    }


    public long getFuel() {
        return tank.getBlock(level).map(tankBlockEntity -> tankBlockEntity.getFluidContainer()
                .getFirstFluid().getFluidAmount()).orElse(0L);
    }

    public long getFuelCapacity() {
        return tank.getBlock(level).map(tankBlockEntity -> tankBlockEntity.getFluidContainer().getTankCapacity(0)).orElse(1L);
    }

    public Component getSpeed() {
        return Component.translatable("tooltip.asteroid_drifter.speed_container", String.format("%,d", (int) speed()));
    }

    public Component getMaxSpeed() {
        return Component.translatable("tooltip.asteroid_drifter.max_speed_container", String.format("%,d", (int) maxSpeed()));
    }

    public Component getAmount() {
        return Component.literal(FluidConstants.toMillibuckets(getFuel())
                + " / " + FluidConstants.toMillibuckets(getFuelCapacity())).withStyle(ChatFormatting.WHITE);
    }

    public Component getRequired(int distance) {
        float v = recipe != null ? recipe.kmToMb(distance) : Float.POSITIVE_INFINITY;
        long fuel = FluidConstants.toMillibuckets(getFuel());
        return Component.translatable("tooltip.asteroid_drifter.fuel_required", fuel, String.format("%.1f", v)).withStyle(v > fuel ? ChatFormatting.RED : ChatFormatting.GRAY);
    }

    public boolean canLaunch(float distance) {
        return recipe != null && FluidConstants.toMillibuckets(getFuel()) > recipe.kmToMb(distance);
    }

    public Component fuelConsumption() {
        if (recipe == null) return Component.empty();
        int km = 1000;
        float mb = recipe.consumption();
        if (mb < 0.1f) {
            km *= 1000;
            mb *= 1000;
        }
        return Component.translatable("tooltip.asteroid_drifter.fuel_container", String.format("%.1f", mb), String.format("%d", km));
    }


    /**
     * Max speed decreased by slagChance
     */
    public float maxLimitedSpeed() {
        return maxLimitedSpeed.getValue();
    }

    /**
     * Max speed without decreased by slagChance
     */
    public float maxSpeed() {
        return maxSpeed.getValue();
    }

    /**
     * Current ship speed
     */
    public float speed() {
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
            Direction direction = controller.getBlock(level).map(c -> c.getBlockState()
                    .getValue(BlockStateProperties.HORIZONTAL_FACING)).orElse(getBlockState()
                    .getValue(BlockStateProperties.HORIZONTAL_FACING));
            if (level.getBlockState(pos).getValue(EngineNozzleBlock.FACING) == direction && nuzzles.add(pos)) {
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

    @Override
    public void setChanged() {
        quickCheckRecipe();
        super.setChanged();
    }

    private void quickCheckRecipe() {
        this.quickCheck.getRecipeFor(this, level).ifPresent((r) -> this.recipe = r);
    }
}
