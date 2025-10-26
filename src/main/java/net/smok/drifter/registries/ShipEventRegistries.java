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
import net.smok.drifter.data.events.AsteroidCollision;
import net.smok.drifter.data.events.ShipEvent;
import net.smok.drifter.data.events.ShipEventType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipEventRegistries {

    public static void init() {}

    public static ResourceKey<Registry<ShipEventType<?>>> COLLISION_TYPE_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Values.MOD_ID, "ship/collision"));


    public static Registry<ShipEventType<?>> COLLISION_TYPES = new MappedRegistry<>(COLLISION_TYPE_KEY, Lifecycle.stable());

    public static final ShipEventType<AsteroidCollision> ASTEROID_COLLISION_TYPE = register("asteroid", AsteroidCollision::codec);

    private static final Map<ResourceLocation, ShipEvent> COLLISIONS = new HashMap<>();

    public static ShipEvent getCollision(ResourceLocation key) {
        return COLLISIONS.get(key);
    }

    public static Pair<ResourceLocation, ShipEvent> getRandomCollision(@NotNull RandomSource random) {
        if (COLLISIONS.isEmpty()) return null;
        List<Map.Entry<ResourceLocation, ShipEvent>> list = COLLISIONS.entrySet().stream().toList();
        Map.Entry<ResourceLocation, ShipEvent> entry = list.get(random.nextInt(list.size()));
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    private static <M extends ShipEvent> ShipEventType<M> register(String name, ShipEventType<M> type) {
        return Registry.register(COLLISION_TYPES, new ResourceLocation(Values.MOD_ID, name), type);
    }

    public static class CollisionRegistration extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

        public CollisionRegistration() {
            super(new Gson(), "ship/events");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            COLLISIONS.clear();

            object.forEach((resourceLocation, jsonElement) -> {
                JsonObject obj = GsonHelper.convertToJsonObject(jsonElement, "events");
                if (obj.has("type")) {
                    JsonElement typeElement = obj.get("type");
                    if (typeElement.isJsonPrimitive()) {
                        ResourceLocation typeId = new ResourceLocation(typeElement.getAsString());
                        ShipEventType<?> eventType = COLLISION_TYPES.get(typeId);
                        if (eventType != null) {
                            Codec<? extends ShipEvent> codec = eventType.codec(resourceLocation);
                            DataResult<? extends ShipEvent> dataResult = codec.parse(JsonOps.INSTANCE, obj);
                            ShipEvent shipEvent = dataResult.getOrThrow(false, Debug::err);
                            COLLISIONS.put(resourceLocation, shipEvent);

                        }
                    }
                }
            });
        }

        @Override
        public ResourceLocation getFabricId() {
            return new ResourceLocation(Values.MOD_ID, "events");
        }
    }
}
