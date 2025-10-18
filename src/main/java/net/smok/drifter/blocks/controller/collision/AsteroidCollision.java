package net.smok.drifter.blocks.controller.collision;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.blocks.structure.ShipStructure;
import net.smok.drifter.entities.CollidedAsteroid;
import net.smok.drifter.registries.CollisionRegistries;
import net.smok.drifter.registries.DrifterEntities;
import org.jetbrains.annotations.NotNull;

public record AsteroidCollision(int iconColor, int minAmount, int maxAmount, int minPower, int maxPower) implements Collision {

    public static final Codec<AsteroidCollision> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("color").forGetter(AsteroidCollision::iconColor),
            Codec.INT.fieldOf("min_amount").forGetter(AsteroidCollision::minAmount),
            Codec.INT.fieldOf("max_amount").forGetter(AsteroidCollision::maxAmount),
            Codec.INT.fieldOf("min_power").forGetter(AsteroidCollision::minPower),
            Codec.INT.fieldOf("max_power").forGetter(AsteroidCollision::maxPower)
    ).apply(instance, AsteroidCollision::new));

    @Override
    public CollisionType<?> getType() {
        return CollisionRegistries.ASTEROID_COLLISION_TYPE;
    }

    @Override
    public void applyCollision(@NotNull Level level, ShipStructure structure) {
        Direction direction = structure.getControllerBlock().map(ShipControllerBlockEntity::backward).orElse(Direction.NORTH);
        BlockPos bigBoxMin = structure.getBigBoxMin();
        BlockPos bigBoxMax = structure.getBigBoxMax();

        RandomSource random = level.random;
        for (BlockPos blockPos : BlockPos.randomBetweenClosed(random, random.nextInt(minAmount, maxAmount),
                bigBoxMin.getX(), bigBoxMin.getY(), bigBoxMin.getZ(),
                bigBoxMax.getX(), bigBoxMax.getY(), bigBoxMax.getZ())) {
            CollidedAsteroid collidedAsteroid = new CollidedAsteroid(DrifterEntities.COLLIDED_ASTEROID.get(), level);
            int power = random.nextInt(minPower, maxPower);
            collidedAsteroid.setPos(blockPos.getCenter());
            Vec3i normal = direction.getNormal();
            Vec3 angle = new Vec3(normal.getX() + random.nextFloat() / 10, normal.getY() + random.nextFloat() / 10, normal.getZ() + random.nextFloat() / 10);
            collidedAsteroid.startTo(angle);
            collidedAsteroid.setExplosionPower(power);

            level.addFreshEntity(collidedAsteroid);
        }
    }
}
