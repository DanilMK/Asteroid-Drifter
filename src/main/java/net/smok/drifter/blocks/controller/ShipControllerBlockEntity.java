package net.smok.drifter.blocks.controller;

import earth.terrarium.adastra.common.blockentities.base.BasicContainer;
import earth.terrarium.botarium.common.menu.ExtraDataMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.registries.DrifterItems;
import net.smok.drifter.registries.Values;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.FlyUtils;
import net.smok.drifter.utils.SavedDataSlot;
import net.smok.drifter.utils.SimpleContainerData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShipControllerBlockEntity extends BlockEntity implements ExtraDataMenuProvider, BasicContainer, ShipBlock {

    
    private static final int ASTEROIDS_AMOUNT = 8;
    private static final int ASTEROID_SCATTER = 30;


    private static final int DANGER_HALF_SIZE = 16;
    private static final int ESCAPE_DANGER_TIME = 60 * 20; // 60 seconds
    private static final int DISTANCE_FACTOR = 50000; // convert UI distance to km


    // start positions for scattering
    private static final int[] startX = new int[] {-88, -8, +72, -88, +92, -88, -8, +72};
    private static final int[] startY = new int[] {-88, -88, -88, -8, -8, +72, +72, +72};


    // Asteroids
    private final AsteroidRecipe[] asteroidRecipes = new AsteroidRecipe[ASTEROIDS_AMOUNT];
    private final NonNullList<ItemStack> items = NonNullList.withSize(ASTEROIDS_AMOUNT, ItemStack.EMPTY);
    private final SimpleContainerData x = new SimpleContainerData(ASTEROIDS_AMOUNT);
    private final SimpleContainerData y = new SimpleContainerData(ASTEROIDS_AMOUNT);
    private final SimpleContainerData dist = new SimpleContainerData(ASTEROIDS_AMOUNT);


    // Local data
    private final SavedDataSlot<Integer> remainDistance = SavedDataSlot.intValue("remainDistance");
    private final SavedDataSlot<Integer> selectedAsteroid = SavedDataSlot.intValue("selectedAsteroid");
    private final SavedDataSlot<Integer> shipPosition = SavedDataSlot.intValue("shipPosition", -50, 50);
    private final SavedDataSlot<Integer> dangerPosition = SavedDataSlot.intValue("dangerPosition", -50, 50);
    private final SavedDataSlot<Collision> collisionType = createDangerTypeValue();
    private final SavedDataSlot<Integer> escapeDangerTime = SavedDataSlot.intValue("escapeDangerTime", 0, ESCAPE_DANGER_TIME); // in ticks
    private final SavedDataSlot<Boolean> stand = SavedDataSlot.booleanValue("stand");
    private int shipMoving;


    private final BlockEntityPosition<AlertPanelBlockEntity> alertPanel =
            new BlockEntityPosition<>("alertPanel", DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get());
    private final BlockEntityPosition<EnginePanelBlockEntity> engine =
            new BlockEntityPosition<>("enginePanel", DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get());


    private final List<SavedDataSlot<?>> savedDataSlots = List.of(remainDistance, selectedAsteroid,
            shipPosition, dangerPosition, collisionType, escapeDangerTime, stand);


    public ShipControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DrifterBlocks.SHIP_CONTROLLER_BLOCK_ENTITY.get(), blockPos, blockState);

        stand.setValue(true);
        createNewAsteroids();
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

    @Override
    public void writeExtraData(ServerPlayer player, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.getBlockPos());
    }

    @Override
    public NonNullList<ItemStack> items() {
        return items;
    }

    public void tick(Level lvl) {
        boolean save = false;
        if (isStand()) return;

        // handle speed
        engine.executeIfPresent(level, block -> block.speedTick(getRemainDistance()));
        save |= leftRightTick();


        if (getRemainDistance() > 0 && getSpeed() > 0) {
            long gameTime = lvl.getGameTime();
            movingTick(lvl, gameTime);
            save |= gameTime % 10 == 0;

        } else {
            land();
        }
        if (save) setChanged();
    }

    private void movingTick(Level lvl, long gameTime) {
        remainDistance.setValue(getRemainDistance() - getSpeed());
        // handle fuel
        engine.executeIfPresent(level, block -> block.fuelTick(gameTime));

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
            return;
        }

        if (items.get(asteroid).isEmpty()) return; // We can't drive to empty asteroid
        if (!engine.getBlock(level).map(e -> e.canLaunch(dist.get(asteroid))).orElse(false)) return; // We can't launch if not enough fuel

        stand.setValue(false);
        selectedAsteroid.setValue(asteroid);
        remainDistance.setValue(dist.get(asteroid));
        shipPosition.setValue(0);
        setChanged();
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
            if (level instanceof ServerLevel serverLevel) {
                ResourceLocation structure = asteroidRecipes[getSelectedAsteroid()].structure();
                StructureTemplate template = serverLevel.getStructureManager().getOrCreate(structure);
                BlockPos place = getBlockPos().above(100);
                serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(place), 1, place);
                template.placeInWorld(serverLevel, place, place, new StructurePlaceSettings(), serverLevel.random, 2);
            }

            remainDistance.setValue(0);
            createNewAsteroids();
        }
        stand.setValue(true);
        engine.executeIfPresent(level, EnginePanelBlockEntity::resetSpeed);
        if (isInDanger()) pass();
        setChanged();
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

        // todo
        
        do for (int i = 0; i < ASTEROIDS_AMOUNT; i++) {
            if (random.nextBoolean()) { // randomly get slot
                AsteroidRecipe recipe = generateAsteroid(random);
                if (recipe == null) continue;
                int xDist = startX[i] + random.nextInt(-ASTEROID_SCATTER, +ASTEROID_SCATTER);
                int yDist = startY[i] + random.nextInt(-ASTEROID_SCATTER, +ASTEROID_SCATTER);
                dist.set(i, (int) (Math.sqrt(xDist * xDist + yDist * yDist) * DISTANCE_FACTOR));
                asteroidRecipes[i] = recipe;
                x.set(i, xDist);
                y.set(i, yDist);
            }
        } while (isEmpty());

        setChanged(level, worldPosition, getBlockState());
    }


    public void moveShip(int value) {
        if (value < 0) shipMoving = -1;
        else if (value > 0) shipMoving = 1;
        else shipMoving = 0;
    }

    private AsteroidRecipe generateAsteroid(RandomSource random) {
        List<AsteroidRecipe> allRecipesFor = level.getRecipeManager().getAllRecipesFor(Values.ASTEROID_RECIPE_TYPE.get())
                .stream().filter(asteroidRecipe -> asteroidRecipe.matches(this, level)).toList();
        return allRecipesFor.get(random.nextInt(allRecipesFor.size()));

    }

    private ItemStack generateItems(@NotNull RandomSource random) {
        return new ItemStack(DrifterItems.MEDIUM_ASTEROID.get());
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
        ContainerHelper.loadAllItems(compoundTag, items);/*
        x.setAll(compoundTag.getIntArray("xPositions"));
        y.setAll(compoundTag.getIntArray("yPositions"));
        dist.setAll(compoundTag.getIntArray("dist"));*/
        alertPanel.load(compoundTag);
        engine.load(compoundTag);

        savedDataSlots.forEach(savedValue -> savedValue.load(compoundTag));

        if (!compoundTag.contains("asteroids", Tag.TAG_LIST)) return;
        ListTag asteroids = compoundTag.getList("asteroids", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < asteroids.size(); i++) {
            CompoundTag tag = (CompoundTag) asteroids.get(i);
            int ax = 0;
            int ay = 0;
            int ad = 0;
            AsteroidRecipe recipe = null;
            if (tag.contains("x", Tag.TAG_INT)) ax = tag.getInt("x");
            if (tag.contains("y", Tag.TAG_INT)) ay = tag.getInt("y");
            if (tag.contains("distance", Tag.TAG_INT)) ad = tag.getInt("distance");
            if (tag.contains("recipe", Tag.TAG_STRING))
                recipe = level.getRecipeManager().getAllRecipesFor(Values.ASTEROID_RECIPE_TYPE.get()).stream()
                        .filter(asteroidRecipe -> asteroidRecipe.id().equals(new ResourceLocation(tag.getString("recipe"))))
                        .findFirst().orElse(null);

            if (ad == 0 || recipe == null) continue;

            asteroidRecipes[i] = recipe;
            x.set(i, ax);
            y.set(i, ay);
            dist.set(i, ad);

        }

    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, items);/*
        compoundTag.putIntArray("xPositions", x.getAll());
        compoundTag.putIntArray("yPositions", y.getAll());
        compoundTag.putIntArray("dist", dist.getAll());*/
        alertPanel.save(compoundTag);
        engine.save(compoundTag);

        savedDataSlots.forEach(savedValue -> savedValue.save(compoundTag));

        ListTag asteroids = new ListTag();
        for (int i = 0; i < ASTEROIDS_AMOUNT; i++) {
            AsteroidRecipe recipe = asteroidRecipes[i];

            if (recipe == null) {
                asteroids.add(new CompoundTag());
                continue;
            }

            int ax = x.get(i);
            int ay = y.get(i);
            int ad = dist.get(i);
            CompoundTag asteroid = new CompoundTag();
            asteroid.putInt("x", ax);
            asteroid.putInt("y", ay);
            asteroid.putInt("distance", ad);
            asteroid.putString("recipe", recipe.id().toString());

            asteroids.add(asteroid);
        }
        compoundTag.put("asteroids", asteroids);
    }


    @Override
    public void clearContent() {
        items.clear();
    }

    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public SimpleContainerData getX() {
        return x;
    }

    public SimpleContainerData getY() {
        return y;
    }

    public SimpleContainerData getDist() {
        return dist;
    }


    public Component getRequired(int distance) {
        return engine.getBlock(level).map(block -> block.getRequired(distance)).orElse(null);
    }

    public long getFuel() {
        return engine.getBlock(level).map(EnginePanelBlockEntity::getFuel).orElse(0L);
    }

    public int getTotalDistance() {
        return dist.get(getSelectedAsteroid());
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
