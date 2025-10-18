package net.smok.drifter.registries;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.controller.collision.AsteroidCollision;
import net.smok.drifter.blocks.controller.collision.Collision;
import net.smok.drifter.blocks.controller.collision.CollisionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionRegistries {

    public static void init() {}

    public static ResourceKey<Registry<CollisionType<?>>> COLLISION_TYPE_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Values.MOD_ID, "ship/collision"));


    public static Registry<CollisionType<?>> COLLISION_TYPES = new MappedRegistry<>(COLLISION_TYPE_KEY, Lifecycle.stable());

    public static final CollisionType<AsteroidCollision> ASTEROID_COLLISION_TYPE = register("asteroid", AsteroidCollision.CODEC);

    private static final Map<ResourceLocation, Collision> COLLISIONS = new HashMap<>();

    public static Collision getCollision(ResourceLocation key) {
        return COLLISIONS.get(key);
    }

    public static Pair<ResourceLocation, Collision> getRandomCollision(@NotNull RandomSource random) {
        if (COLLISIONS.isEmpty()) return null;
        List<Map.Entry<ResourceLocation, Collision>> list = COLLISIONS.entrySet().stream().toList();
        Map.Entry<ResourceLocation, Collision> entry = list.get(random.nextInt(list.size()));
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    private static <M extends Collision> CollisionType<M> register(String name, Codec<M> codec) {
        return Registry.register(COLLISION_TYPES, new ResourceLocation(Values.MOD_ID, name), () -> codec);
    }

    public static class CollisionRegistration extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

        public CollisionRegistration() {
            super(new Gson(), "ship/collisions");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            COLLISIONS.clear();

            object.forEach((resourceLocation, jsonElement) -> {
                JsonObject obj = GsonHelper.convertToJsonObject(jsonElement, "collisions");
                DataResult<Collision> parse = Collision.CODEC.parse(JsonOps.INSTANCE, obj);
                Collision collision = parse.getOrThrow(false, Debug::err);
                COLLISIONS.put(resourceLocation, collision);
                Debug.log("Object in " + resourceLocation);
            });
            Debug.log("Load objects: " + COLLISIONS.size());
        }

        @Override
        public ResourceLocation getFabricId() {
            return new ResourceLocation(Values.MOD_ID, "ship/collisions");
        }
    }
}
