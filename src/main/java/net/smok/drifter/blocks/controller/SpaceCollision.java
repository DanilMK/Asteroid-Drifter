package net.smok.drifter.blocks.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.smok.drifter.blocks.structure.ShipStructure;
import net.smok.drifter.entities.CollidedAsteroid;
import net.smok.drifter.registries.DrifterEntities;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum SpaceCollision {
    NONE(0, 0, 0, 0, 1, 1),
    ONE_SMALL(1, 0, 1, 1, 3, 7),
    ONE_BIG(2, 1, 1, 1, 5, 10),
    RADIATION(3, 0, 0, 0, 1, 1),
    CLUSTER(4, 1, 3, 7, 3, 7),
    MAGNETIC(5, 0, 0, 0, 1, 1),

    ;


    public final int type;
    public final int dangerIcon;
    public final int minCount;
    public final int maxCount;
    public final int minRadius;
    public final int maxRadius;

    SpaceCollision(int type, int dangerIcon, int minCount, int maxCount, int minRadius, int maxRadius) {
        this.type = type;
        this.dangerIcon = dangerIcon;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
    }

    public void createExplosion(@NotNull Level level, ShipStructure structure) {
        if (maxCount == 0) return;
        int count = level.random.nextInt(minCount, maxCount);
        for (BlockPos pos : structure.getRandomInAnyBox(level.random, count)) {
            Explosion explode = level.explode(null, null, null, pos.getCenter(), level.random.nextInt(minRadius, maxRadius), false, Level.ExplosionInteraction.BLOCK);
            explode.explode();
        }
    }


    public static SpaceCollision of(int value) {
        return switch (value) {
            case 0 -> NONE;
            case 1 -> ONE_BIG;
            case 2 -> ONE_SMALL;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }

    public static SpaceCollision random(@NotNull RandomSource randomSource) {
        return SpaceCollision.values()[randomSource.nextInt(1, SpaceCollision.values().length)];
    }

    public record AsteroidCollisionHandler(int minAmount, int maxAmount, int minPower, int maxPower) implements CollisionHandler {

        @Override
        public void handleCollision(@NotNull Level level, ShipStructure structure) {
            Direction direction = structure.getControllerBlock().map(ShipControllerBlockEntity::backward).orElse(Direction.NORTH);
            BlockPos bigBoxMin = structure.getBigBoxMin();
            BlockPos bigBoxMax = structure.getBigBoxMax();


            RandomSource random = level.random;
            for (BlockPos blockPos : BlockPos.randomBetweenClosed(random, random.nextInt(minAmount, maxAmount),
                    bigBoxMin.getX(), bigBoxMin.getY(), bigBoxMin.getZ(),
                    bigBoxMax.getX(), bigBoxMax.getY(), bigBoxMax.getZ())) {
                CollidedAsteroid collidedAsteroid = new CollidedAsteroid(DrifterEntities.COLLIDED_ASTEROID.get(), level);
                collidedAsteroid.setPos(blockPos.getCenter());
                Vec3i normal = direction.getNormal();
                collidedAsteroid.startTo(new Vec3(normal.getX(), normal.getY(), normal.getZ()));
                level.addFreshEntity(collidedAsteroid);
            }
        }
    }

    public interface CollisionHandler {
        void handleCollision(@NotNull Level level, ShipStructure structure);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull SavedDataSlot<SpaceCollision> createDangerTypeValue() {
        return new SavedDataSlot<>(NONE) {
            @Override
            public void load(CompoundTag compoundTag) {
                setValue(SpaceCollision.valueOf(compoundTag.getString("dangerType").toUpperCase()));
            }

            @Override
            public void save(CompoundTag compoundTag) {
                compoundTag.putString("dangerType", getValue().name().toLowerCase());
            }

            @Override
            public int get() {
                return getValue().type;
            }

            @Override
            public void set(int i) {
                setValue(of(i));
            }
        };
    }

}
