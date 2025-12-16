package net.smok.drifter.registries;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.smok.drifter.entities.CollidedAsteroid;
import net.smok.drifter.entities.MartianMandrake;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DrifterEntities {

    public static void init() {
        registerAttributes((supplier, builderSupplier) -> FabricDefaultAttributeRegistry.register(supplier.get(), builderSupplier.get()));
    }

    public static final ResourcefulRegistry<EntityType<?>> ENTITY_TYPES = ResourcefulRegistries.create(BuiltInRegistries.ENTITY_TYPE, Values.MOD_ID);

    public static final RegistryEntry<EntityType<CollidedAsteroid>> COLLIDED_ASTEROID = ENTITY_TYPES.register("collided_asteroid",
            () -> builder(CollidedAsteroid::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("collided_asteroid")
    );

    public static final RegistryEntry<EntityType<MartianMandrake>> MARTIAN_MANDRAKE = ENTITY_TYPES.register("martian_mandrake",
            () -> builder(MartianMandrake::new, MobCategory.AMBIENT)
                    .sized(0.5f, 0.6f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("martian_mandrake"));
/*
    public static final RegistryEntry<EntityType<MagneticField>> MAGNETIC_FIELD =
            ENTITY_TYPES.register("magnetic_field", () -> builder(MagneticField::new, MobCategory.MISC)
                    .sized(1, 1)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("magnetic_field")
            );*/


    public static <T extends Entity> EntityType.Builder<T> builder(Function<Level, T> factory, MobCategory category) {
        return EntityType.Builder.of((entityType, level) -> factory.apply(level), category);
    }

    public static <T extends Entity> EntityType.Builder<T> builder(BiFunction<EntityType<T>, Level, T> factory, MobCategory category) {
        return EntityType.Builder.of(factory::apply, category);
    }

    private static void registerAttributes(BiConsumer<Supplier<? extends EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier.Builder>> attributes) {
        attributes.accept(MARTIAN_MANDRAKE, MartianMandrake::createAttributes);
    }

}