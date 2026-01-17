package net.smok.drifter.blocks.controller;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.ShipConfig;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.blocks.alert.Alert;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.blocks.alert.Detector;
import net.smok.drifter.blocks.controller.extras.ComplexPathGenerator;
import net.smok.drifter.blocks.controller.extras.FutureEventsContainer;
import net.smok.drifter.events.ShipEvent;
import net.smok.drifter.blocks.controller.extras.SimpleLandLaunchHandler;
import net.smok.drifter.blocks.structure.ShipStructure;
import net.smok.drifter.recipies.Path;
import net.smok.drifter.menus.ShipControllerMenu;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShipControllerBlockEntity extends ExtendedBlockEntity implements ShipBlock, Detector {


    private static final int DANGER_HALF_SIZE = 16;
    private static final int ESCAPE_DANGER_TIME = 60 * 20; // 60 seconds


    // Asteroids
    private final List<Path> paths = new ArrayList<>();


    // Local data
    private final SavedDataSlot<Float> remainDistance = SavedDataSlot.floatValue("remainDistance");
    private final SavedDataSlot<Integer> selectedAsteroid = SavedDataSlot.intValue("selectedAsteroid");
    private final SavedDataSlot<Integer> shipPosition = SavedDataSlot.intValue("shipPosition", -50, 50);
    private final SavedDataSlot<Integer> dangerPosition = SavedDataSlot.intValue("dangerPosition", -50, 50);
    private final SavedDataSlot<Pair<ResourceLocation, ShipEvent>> collisionType = ShipEvent.createSavedData();
    private final SavedDataSlot<Integer> escapeDangerTime = SavedDataSlot.intValue("escapeDangerTime", 0, ESCAPE_DANGER_TIME); // in ticks
    private final SavedDataSlot<Boolean> stand = SavedDataSlot.booleanValue("stand");
    private final SavedDataSlot<Integer> completedEvents = SavedDataSlot.intValue("completedEvents");
    private int shipMoving;
    private int clientTick = 20;

    private final PathGenerator pathGenerator;
    private final FutureEventsContainer futureEventsContainer;
    private LandLaunchHandler landLaunchHandler;


    private final BlockEntityPosition<AlertPanelBlockEntity> alertPanel =
            new BlockEntityPosition<>("alertPanel", DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get());
    private final BlockEntityPosition<EnginePanelBlockEntity> engine =
            new BlockEntityPosition<>("enginePanel", DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get());


    private final List<SavedDataSlot<?>> savedDataSlots = List.of(remainDistance, selectedAsteroid,
            shipPosition, dangerPosition, collisionType, escapeDangerTime, stand, completedEvents);


    public ShipControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get(), blockPos, blockState);
        pathGenerator = new ComplexPathGenerator();
        landLaunchHandler = new SimpleLandLaunchHandler(getBlockPos(), 25, 4);
        futureEventsContainer = new FutureEventsContainer(this);

        stand.setValue(true);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.asteroid_drifter.ship_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ShipControllerMenu(i, inventory, this);
    }

    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public void clientTick() {
        if (clientTick < 20) {
            clientTick++;
        }
    }

    public void tick(ServerLevel serverLevel) {
        boolean save = false;
        landLaunchHandler.destroyOnLaunch(serverLevel);
        if (isStand()) return;
        if (serverLevel.getGameTime() % 80L == 0L) playSound(SoundEvents.BEACON_AMBIENT);

        // handle speed
        save |= leftRightTick();


        if (getRemainDistance() > 0) {
            long gameTime = serverLevel.getGameTime();
            movingTick(serverLevel);
            save |= gameTime % 10 == 0;

        } else {
            land();
        }
        if (save) setChanged();
    }

    private void movingTick(Level lvl) {

        engine.executeIfPresent(level, block -> block.speedTick(getRemainDistance()));
        remainDistance.setValue(getRemainDistance() - ShipConfig.tickToMin(getSpeed()));

        ShipStructure.findStructure(lvl, getBlockPos()).ifPresent(structure ->
                getSelectedRecipe().startEvent((int) getRemainDistance(), lvl, structure));

        if (getSelectedRecipe() != null) {
            boolean b = getSelectedRecipe().anyAlert();
            lvl.setBlock(getBlockPos().above(), Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, b), 3);
        }
    }


    private boolean leftRightTick() {
        if (shipMoving == 0) return false;
        shipPosition.setValue(shipPosition.getValue() + shipMoving);
        shipMoving = 0;
        return true;
    }


    public void launch(int asteroid) {
        if (isLaunch()) return;
        if (getRemainDistance() > 0 && getSpeed() == 0) {
            if (engine.getBlock(level).map(e -> e.canLaunch(getRemainDistance())).orElse(false)) {
                stand.setValue(false);
                playSound(SoundEvents.BEACON_ACTIVATE);
                setChanged();
                return;
            }
        }

        if (asteroid >= paths.size() || asteroid < 0) return;
        Path recipe = paths.get(asteroid);

        if (recipe.getRecipe(level).isEmpty()) return; // We can't drive to empty asteroid todo peredelat
        if (!engine.getBlock(level).map(e -> e.canLaunch(recipe.distance())).orElse(false)) return; // We can't launch if not enough fuel

        stand.setValue(false);
        landLaunchHandler.startDestroy();
        selectedAsteroid.setValue(asteroid);
        remainDistance.setValue((float) getSelectedRecipe().distance());
        shipPosition.setValue(0);
        setChanged();
        playSound(SoundEvents.BEACON_ACTIVATE);
        setBlockStateLaunch(true);
    }



    public void land() {
        if (isStand()) return;
        if (getRemainDistance() < 100) {
            if (level instanceof ServerLevel serverLevel) paths.get(getSelectedAsteroid()).getRecipe(level).ifPresent(recipe -> {
                landLaunchHandler = new SimpleLandLaunchHandler(getBlockPos(),
                        ShipStructure.findStructure(level, getBlockPos()).map(value -> value.getBigBoxMin().getY()).orElse(25), recipe.size());
                landLaunchHandler.placeOnLand(serverLevel, recipe);
            });
            remainDistance.setValue(0f);
            reRollPaths();
            completedEvents.setValue(0);
        }
        stand.setValue(true);
        engine.executeIfPresent(level, EnginePanelBlockEntity::resetSpeed);
        setChanged();
        SoundEvent sound = SoundEvents.BEACON_DEACTIVATE;
        playSound(sound);
        setBlockStateLaunch(false);
    }


    public boolean isInDanger() {
        return getCollision() != null &&
                shipPosition.getValue() > (dangerPosition.getValue() - DANGER_HALF_SIZE) &&
                shipPosition.getValue() < (dangerPosition.getValue() + DANGER_HALF_SIZE);
    }

    public void reRollPaths() {
        if (isLaunch()) return; // While we are driving we can't recreate asteroids
        if (level == null) return;
        RandomSource random = level.getRandom();

        paths.clear();
        pathGenerator.genAsteroidField(this, random, paths);
        selectedAsteroid.setValue(0);

        setChanged();
    }


    public void moveShip(int value) {
        shipMoving = Integer.compare(value, 0);
    }


    public void creativeControl(int command) {
        switch (command) {
            case 0 -> reRollPaths();
            case 1 -> land();
            case 2 -> {
                remainDistance.setValue(0f);
                land();
            }
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        alertPanel.load(compoundTag);
        engine.load(compoundTag);
        futureEventsContainer.load(compoundTag);

        boolean b = isStand();
        savedDataSlots.forEach(savedValue -> savedValue.load(compoundTag));
        if (b != isStand()) clientTick = 0;
        paths.clear();

        if (!compoundTag.contains("asteroids", Tag.TAG_LIST)) return;
        ListTag recipeList = compoundTag.getList("asteroids", CompoundTag.TAG_COMPOUND);
        for (Tag value : recipeList) {
            CompoundTag tag = (CompoundTag) value;
            Path element = Path.loadData(futureEventsContainer, tag);
            if (element != null) paths.add(element);
        }


    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        alertPanel.save(compoundTag);
        engine.save(compoundTag);
        futureEventsContainer.save(compoundTag);

        savedDataSlots.forEach(savedValue -> savedValue.save(compoundTag));

        ListTag asteroids = new ListTag();
        this.paths.forEach(placedAsteroidRecipe -> asteroids.add(placedAsteroidRecipe.saveData()));

        compoundTag.put("asteroids", asteroids);
    }

    public FutureEventsContainer getFutureEventsContainer() {
        return futureEventsContainer;
    }

    public Path getSelectedRecipe() {
        return paths.get(selectedAsteroid.getValue());
    }

    public int getCompletedEvents() {
        return completedEvents.getValue();
    }

    private void playSound(SoundEvent beaconActivate) {
        level.playSound(null, getBlockPos(), beaconActivate, SoundSource.BLOCKS, 2.0F, 0.5F);
    }

    private void setBlockStateLaunch(boolean launch) {
        level.setBlock(getBlockPos(), getBlockState().setValue(ShipControllerBlock.LAUNCH, launch), 2);
    }

    public Component getRequired(int distance) {
        return engine.getBlock(level).map(block -> block.getRequired(distance)).orElse(null);
    }

    public float getTotalDistance() {
        return getSelectedRecipe().distance();
    }

    public float getRemainDistance() {
        return remainDistance.getValue();
    }

    public int getSelectedAsteroid() {
        return selectedAsteroid.getValue();
    }

    public int getDangerPosition() {
        return dangerPosition.getValue();
    }

    public float getSpeed() {
        return engine.getBlock(level).map(EnginePanelBlockEntity::speed).orElse(0f);
    }

    public float maxSpeed() {
        return engine.getBlock(level).map(EnginePanelBlockEntity::maxLimitedSpeed).orElse(1f);
    }

    public Boolean isStand() {
        return stand.getValue();
    }

    public boolean isLaunch() {
        return !stand.getValue();
    }

    public List<SavedDataSlot<?>> getDataSlots() {
        return savedDataSlots;
    }

    public int getShipPosition() {
        return shipPosition.getValue();
    }

    public Pair<ResourceLocation, ShipEvent> getCollision() {
        return collisionType.getValue();
    }

    public int getClientTick() {
        return clientTick;
    }

    @Override
    public boolean bind(BlockPos pos, ShipBlock other) {
        if (other instanceof AlertPanelBlockEntity) {
            alertPanel.setPos(pos);
            return true;
        }
        if (other instanceof EnginePanelBlockEntity) {
            engine.setPos(pos);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        alertPanel.setPos(null);
        engine.setPos(null);
    }

    public List<Path> getAllPaths() {
        return paths;
    }

    public BlockEntityPosition<EnginePanelBlockEntity> getEnginePanelBlock() {
        return engine;
    }

    public Direction forward() {
        return getBlockState().getValue(ShipControllerBlock.FACING).getOpposite();
    }

    public Direction backward() {
        return getBlockState().getValue(ShipControllerBlock.FACING);
    }

    @Override
    public List<Alert> getAllAlerts() {
        List<Alert> alerts = new ArrayList<>();
        if (isLaunch() && getSelectedRecipe() != null) alerts.addAll(getSelectedRecipe().getActiveAlerts());
        alerts.addAll(futureEventsContainer.getAllAlerts());
        return alerts;
    }
}
