package net.smok.drifter.blocks.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.blocks.controller.extras.SimpleAsteroidFieldGenerator;
import net.smok.drifter.blocks.controller.extras.SimpleLandLaunchHandler;
import net.smok.drifter.recipies.PlacedAsteroidRecipe;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.FlyUtils;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShipControllerBlockEntity extends ExtendedBlockEntity implements ShipBlock {

    
    private static final int ASTEROIDS_AMOUNT = 8;


    private static final int DANGER_HALF_SIZE = 16;
    private static final int ESCAPE_DANGER_TIME = 60 * 20; // 60 seconds


    // Asteroids
    private final List<PlacedAsteroidRecipe> asteroids = NonNullList.withSize(ASTEROIDS_AMOUNT, PlacedAsteroidRecipe.EMPTY);


    // Local data
    private final SavedDataSlot<Integer> remainDistance = SavedDataSlot.intValue("remainDistance");
    private final SavedDataSlot<Integer> selectedAsteroid = SavedDataSlot.intValue("selectedAsteroid");
    private final SavedDataSlot<Integer> shipPosition = SavedDataSlot.intValue("shipPosition", -50, 50);
    private final SavedDataSlot<Integer> dangerPosition = SavedDataSlot.intValue("dangerPosition", -50, 50);
    private final SavedDataSlot<Collision> collisionType = createDangerTypeValue();
    private final SavedDataSlot<Integer> escapeDangerTime = SavedDataSlot.intValue("escapeDangerTime", 0, ESCAPE_DANGER_TIME); // in ticks
    private final SavedDataSlot<Boolean> stand = SavedDataSlot.booleanValue("stand");
    private int shipMoving;

    private final AsteroidFieldGenerator asteroidFieldGenerator;
    private final LandLaunchHandler landLaunchHandler;


    private final BlockEntityPosition<AlertPanelBlockEntity> alertPanel =
            new BlockEntityPosition<>("alertPanel", DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get());
    private final BlockEntityPosition<EnginePanelBlockEntity> engine =
            new BlockEntityPosition<>("enginePanel", DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get());


    private final List<SavedDataSlot<?>> savedDataSlots = List.of(remainDistance, selectedAsteroid,
            shipPosition, dangerPosition, collisionType, escapeDangerTime, stand);


    public ShipControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get(), blockPos, blockState);
        asteroidFieldGenerator = new SimpleAsteroidFieldGenerator();
        landLaunchHandler = new SimpleLandLaunchHandler(getBlockPos(), 25);

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

    public void tick(Level clientLevel) {
        asteroids.forEach(recipe -> recipe.setRecipe(clientLevel));
    }

    public void tick(ServerLevel serverLevel) {
        boolean save = false;
        asteroids.forEach(recipe -> recipe.setRecipe(serverLevel));
        landLaunchHandler.destroyOnLaunch(serverLevel);
        if (isStand()) return;
        if (serverLevel.getGameTime() % 80L == 0L) playSound(SoundEvents.BEACON_AMBIENT);

        // handle speed
        save |= leftRightTick();


        if (getRemainDistance() > 0 && getSpeed() > 0) {
            long gameTime = serverLevel.getGameTime();
            movingTick(serverLevel, gameTime);
            save |= gameTime % 10 == 0;

        } else {
            land();
        }
        if (save) setChanged();
    }

    private void movingTick(Level lvl, long gameTime) {
        remainDistance.setValue(getRemainDistance() - getSpeed());
        // handle fuel

        float leftTime = FlyUtils.leftTime(maxSpeed(), getRemainDistance(), getSpeed());
        if (getCollision() == Collision.NONE && gameTime % 120 == 0 && leftTime > ESCAPE_DANGER_TIME) { //todo increase
            int chance = lvl.getRandom().nextInt(0, 100);
            if (chance < 5) createDanger(Collision.of(lvl.getRandom().nextInt(1, 3)), lvl);
        }

        if (getCollision() != Collision.NONE) {
            if (escapeDangerTime.getValue() > 0) {
                escapeDangerTime.setValue(escapeDangerTime.getValue() - 1);
                if (isInDanger() && gameTime % 20L == 0L) alertPanel.executeIfPresent(lvl, alertPanelBlock ->
                        alertPanelBlock.startDanger(getCollision()));

            } else if (isInDanger()) crush();
            else pass();
        }
    }


    private boolean leftRightTick() {
        if (shipMoving == 0) return false;
        shipPosition.setValue(shipPosition.getValue() + shipMoving);
        shipMoving = 0;
        return true;
    }

    private void pass() {
        Debug.log("Pass danger on " + level);
        collisionType.setValue(Collision.NONE);
        setChanged();
    }

    private void crush() {
        Debug.log("Crush " + getCollision() + " on " + level);
        collisionType.setValue(Collision.NONE);
        setChanged();
    }

    public void launch(int asteroid) {
        if (!isStand()) return; // While we are driving we can't launch again
        if (getRemainDistance() > 0 && getSpeed() == 0) {
            if (engine.getBlock(level).map(e -> e.canLaunch(getRemainDistance())).orElse(false)) stand.setValue(false);
        }

        if (asteroid >= asteroids.size() || asteroid < 0) return;
        PlacedAsteroidRecipe recipe = asteroids.get(asteroid);

        if (recipe.recipe().isEmpty()) return; // We can't drive to empty asteroid
        if (!engine.getBlock(level).map(e -> e.canLaunch(recipe.distance())).orElse(false)) return; // We can't launch if not enough fuel

        stand.setValue(false);
        landLaunchHandler.startDestroy();
        selectedAsteroid.setValue(asteroid);
        remainDistance.setValue(getSelectedRecipe().distance());
        shipPosition.setValue(0);
        setChanged();
        playSound(SoundEvents.BEACON_ACTIVATE);
        setBlockStateLaunch(true);
    }

    public void createDanger(Collision danger, Level lvl) {
        if (isStand()) return; // can't handle danger in standing position
        if (getCollision() != Collision.NONE) return;
        dangerPosition.setValue(shipPosition.getValue());
        collisionType.setValue(danger);
        escapeDangerTime.setValue(ESCAPE_DANGER_TIME);
        alertPanel.executeIfPresent(lvl, alertPanelBlock ->
                alertPanelBlock.startDanger(getCollision()));
    }

    public void land() {
        if (getRemainDistance() < 100) {
            if (level instanceof ServerLevel serverLevel) asteroids.get(getSelectedAsteroid()).recipe().ifPresent(recipe -> landLaunchHandler.placeOnLand(serverLevel, recipe));
            remainDistance.setValue(0);
            createNewAsteroids();
        }
        stand.setValue(true);
        engine.executeIfPresent(level, EnginePanelBlockEntity::resetSpeed);
        if (isInDanger()) pass();
        setChanged();
        SoundEvent sound = SoundEvents.BEACON_DEACTIVATE;
        playSound(sound);
        setBlockStateLaunch(false);
    }


    public boolean isInDanger() {
        return getCollision() != Collision.NONE &&
                shipPosition.getValue() > (dangerPosition.getValue() - DANGER_HALF_SIZE) &&
                shipPosition.getValue() < (dangerPosition.getValue() + DANGER_HALF_SIZE);
    }

    public static boolean isInDanger(Collision collision, int shipPosition, int dangerPosition) {
        return collision != Collision.NONE &&
                shipPosition > (dangerPosition - DANGER_HALF_SIZE) &&
                shipPosition < (dangerPosition + DANGER_HALF_SIZE);
    }

    public void createNewAsteroids() {
        if (!isStand()) return; // While we are driving we can't recreate asteroids
        if (level == null) return;
        RandomSource random = level.getRandom();

        asteroids.clear();
        asteroidFieldGenerator.genAsteroidField(this, random, asteroids);

        setChanged();
    }


    public void moveShip(int value) {
        shipMoving = Integer.compare(value, 0);
    }


    public void creativeControl(int command) {
        switch (command) {
            case 0 -> createNewAsteroids();
            case 1 -> land();
            case 2 -> {
                remainDistance.setValue(0);
                land();
            }
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        alertPanel.load(compoundTag);
        engine.load(compoundTag);

        savedDataSlots.forEach(savedValue -> savedValue.load(compoundTag));

        if (!compoundTag.contains("asteroids", Tag.TAG_LIST)) return;
        ListTag recipeList = compoundTag.getList("asteroids", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < recipeList.size() && i < this.asteroids.size(); i++) {
            CompoundTag tag = (CompoundTag) recipeList.get(i);
            PlacedAsteroidRecipe placedAsteroidRecipe = PlacedAsteroidRecipe.loadData(tag);
            this.asteroids.set(i, placedAsteroidRecipe);
        }

        if (level != null) asteroids.forEach(recipe -> recipe.setRecipe(level));

    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        alertPanel.save(compoundTag);
        engine.save(compoundTag);

        savedDataSlots.forEach(savedValue -> savedValue.save(compoundTag));

        ListTag asteroids = new ListTag();
        this.asteroids.forEach(placedAsteroidRecipe -> asteroids.add(placedAsteroidRecipe.saveData()));

        compoundTag.put("asteroids", asteroids);
    }


    public PlacedAsteroidRecipe getSelectedRecipe() {
        return asteroids.get(selectedAsteroid.getValue());
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

    public long getFuel() {
        return engine.getBlock(level).map(EnginePanelBlockEntity::getFuel).orElse(0L);
    }

    public int getTotalDistance() {
        return getSelectedRecipe().distance();
    }

    public int getRemainDistance() {
        return remainDistance.getValue();
    }

    public int getSelectedAsteroid() {
        return selectedAsteroid.getValue();
    }

    public int getDangerPosition() {
        return dangerPosition.getValue();
    }

    public int getSpeed() {
        return engine.getBlock(level).map(EnginePanelBlockEntity::speed).orElse(0);
    }

    public int maxSpeed() {
        return engine.getBlock(level).map(EnginePanelBlockEntity::maxSpeed).orElse(1);
    }

    public int getFuelEfficiency() {
        return engine.getBlock(level).map(EnginePanelBlockEntity::getFuelEfficiency).orElse(0);
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

    public Collision getCollision() {
        return collisionType.getValue();
    }


    @Override
    public boolean  bind(BlockPos pos, ShipBlock other) {
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

    public List<PlacedAsteroidRecipe> getAllRecipes() {
        return asteroids;
    }

    public enum Collision {
        NONE(0), BIG(1), SMALL(2);
        public final int type;

        Collision(int type) {
            this.type = type;
        }

        static Collision of(int value) {
            return switch (value) {
                case 0 -> NONE;
                case 1 -> BIG;
                case 2 -> SMALL;
                default -> throw new IllegalStateException("Unexpected value: " + value);
            };
        }
    }

    @Contract(value = " -> new", pure = true)
    private static @NotNull SavedDataSlot<Collision> createDangerTypeValue() {
        return new SavedDataSlot<>(Collision.NONE) {
            @Override
            public void load(CompoundTag compoundTag) {
                set(compoundTag.getInt("dangerType"));
            }

            @Override
            public void save(CompoundTag compoundTag) {
                compoundTag.putInt("dangerType", get());
            }

            @Override
            public int get() {
                return getValue().type;
            }

            @Override
            public void set(int i) {
                setValue(Collision.of(i));
            }
        };
    }

    public BlockEntityPosition<EnginePanelBlockEntity> getEnginePanelBlock() {
        return engine;
    }
}
