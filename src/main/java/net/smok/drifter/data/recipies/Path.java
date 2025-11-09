package net.smok.drifter.data.recipies;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.smok.drifter.data.events.ShipEvent;
import net.smok.drifter.registries.ShipEventRegistries;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record Path(@NotNull ResourceLocation recipeId, int x, int y, int distance, int ring, @NotNull List<PathEvent> pathEvents) {

    private record PathEvent(ResourceLocation eventId, int startOn) {

        public @Nullable ShipEvent getEvent() {
            return ShipEventRegistries.getCollision(eventId);
        }

        public @NotNull CompoundTag saveData() {
            CompoundTag tag = new CompoundTag();
            tag.putString("event", eventId.toString());
            tag.putInt("startOn", startOn);
            return tag;
        }

        public static @Nullable PathEvent loadData(@NotNull CompoundTag tag) {
            if (tag.contains("event", ListTag.TAG_STRING) && tag.contains("startOn", Tag.TAG_INT))
                return new PathEvent(new ResourceLocation(tag.getString("event")), tag.getInt("startOn"));
            return null;
        }
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull Path of(@NotNull AsteroidRecipe recipe, int x, int y, int distance, int ring, @NotNull List<Pair<ShipEvent, Integer>> events) {
        return new Path(recipe.id(), x, y, distance, ring, events.stream().map(pair -> new PathEvent(pair.getFirst().id(), pair.getSecond()))
                .sorted(Comparator.comparing(pathEvent -> pathEvent.startOn)).toList());
    }


    public @NotNull Optional<AsteroidRecipe> getRecipe(@NotNull Level level) {
        return level.getRecipeManager().getAllRecipesFor(Values.ASTEROID_RECIPE_TYPE.get()).stream()
                .filter(recipe -> recipe.id().equals(recipeId)).findFirst();
    }

    public @NotNull CompoundTag saveData() {
        CompoundTag tag = new CompoundTag();

        tag.putString("recipe", recipeId().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("ring", ring);
        tag.putInt("distance", distance);
        tag.put("pathEvents", pathEvents.stream().map(PathEvent::saveData).collect(Collectors.toCollection(ListTag::new)));

        return tag;
    }

    public static @Nullable Path loadData(@NotNull CompoundTag tag) {
        if (!tag.contains("recipe", Tag.TAG_STRING) || !tag.contains("distance", Tag.TAG_INT)) return null;

        ResourceLocation recipeId = new ResourceLocation(tag.getString("recipe"));
        int distance = tag.getInt("distance");

        int x = tag.contains("x", Tag.TAG_INT) ? tag.getInt("x") : 0;
        int y = tag.contains("y", Tag.TAG_INT) ? tag.getInt("y") : 0;
        int ring = tag.contains("ring", Tag.TAG_INT) ? tag.getInt("ring") : 0;

        List<PathEvent> events;
        if (tag.contains("pathEvents", Tag.TAG_LIST)) {
            events = new ArrayList<>();
            for (Tag pathEventTag : tag.getList("pathEvents", Tag.TAG_COMPOUND)) {
                PathEvent pathEvent = PathEvent.loadData((CompoundTag) pathEventTag);
                if (pathEvent != null) events.add(pathEvent);
            }
        } else events = List.of();

        return new Path(recipeId, x, y, distance, ring, events);
    }

    public @NotNull Optional<ShipEvent> startEvent(int distance, int completedEvents) {
        List<ShipEvent> list = pathEvents.stream().filter(pathEvent -> pathEvent.startOn < distance && pathEvent.getEvent() != null).map(PathEvent::getEvent).toList();
        if (list.size() > completedEvents) return Optional.ofNullable(list.get(completedEvents));
        return Optional.empty();
    }

}
