package net.smok.drifter.recipies;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.smok.drifter.ShipConfig;
import net.smok.drifter.blocks.alert.Alert;
import net.smok.drifter.blocks.alert.AlertSound;
import net.smok.drifter.blocks.alert.Icon;
import net.smok.drifter.blocks.controller.extras.FutureEventsContainer;
import net.smok.drifter.blocks.structure.ShipStructure;
import net.smok.drifter.events.ShipEvent;
import net.smok.drifter.registries.DrifterRecipes;
import net.smok.drifter.registries.ShipEventRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record Path(@NotNull ResourceLocation recipeId, int x, int y, int distance, int ring, @NotNull List<OnPathEvent> onPathEvents) {

    private static final class OnPathEvent extends Alert {
        private final ResourceLocation eventId;
        private final float alertTime;
        private final int startOn;
        private final FutureEventsContainer container;
        private boolean complete;

        private OnPathEvent(FutureEventsContainer container, String alertName, Icon alertIcon, ResourceLocation eventId, int startOn, float alertTime) {
            super(container, alertIcon, alertName);
            this.container = container;
            this.eventId = eventId;
            this.startOn = startOn;
            this.alertTime = alertTime;
            this.complete = false;
        }

            public @Nullable ShipEvent getEvent() {
                return ShipEventRegistries.getShipEvent(eventId);
            }

            public @NotNull CompoundTag saveData() {
                CompoundTag tag = new CompoundTag();
                tag.putString("event", eventId.toString());
                tag.putInt("startOn", startOn);
                tag.putString("name", name);
                tag.putFloat("alert_time", alertTime);
                tag.putBoolean("complete", complete);
                icon.save(tag);
                return tag;
            }

            public static @Nullable Path.OnPathEvent loadData(FutureEventsContainer container, @NotNull CompoundTag tag) {
                if (tag.contains("event", ListTag.TAG_STRING) && tag.contains("startOn", Tag.TAG_INT) &&
                        tag.contains("name", Tag.TAG_STRING) && tag.contains("complete")) {

                    OnPathEvent onPathEvent = new OnPathEvent(container, tag.getString("name"), new Icon(),
                            new ResourceLocation(tag.getString("event")), tag.getInt("startOn"),
                            tag.contains("alert_time", Tag.TAG_FLOAT) ? tag.getFloat("alert_time") : 0);
                    if (tag.contains("complete")) onPathEvent.complete = tag.getBoolean("complete");
                    onPathEvent.icon.load(tag);
                    return onPathEvent;
                }
                return null;
            }

        @Override
        public void setSound(AlertSound sound) {
            if (container.getCommonSound().equals(sound)) return;
            container.setSound(sound);
            container.setChanged();
        }

        @Override
        public @NotNull Component subText() {
            return Component.literal(ShipConfig.timeToString(leftTime()));
        }

        @Override
        public AlertSound getSound() {
            return container.getCommonSound();
        }

        @Override
        public boolean canEditName() {
            return false;
        }

        @Override
        public boolean canEditIcon() {
            return false;
        }

        @Override
        public boolean canBeTested() {
            return false;
        }

        @Override
        public boolean isActive() {
            return leftTime() < alertTime;
        }

        public float leftTime() {
            return (container.getRemainDistance() - startOn) / container.maxSpeed();
        }

        @Override
        public String toString() {
            return "OnPathEvent{" +
                    "eventId=" + eventId +
                    ", startOn=" + startOn +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public List<Alert> getActiveAlerts() {
        return onPathEvents.stream().filter(onPathEvent1 -> onPathEvent1.isActive() && !onPathEvent1.complete)
                .map(onPathEvent -> (Alert) onPathEvent).toList();
    }


    @Contract("_, _, _, _, _, _, _ -> new")
    public static @NotNull Path of(FutureEventsContainer container, @NotNull AsteroidRecipe recipe, int x, int y,
                                   int distance, int ring, @NotNull List<Pair<PathEvent, Integer>> events) {

        return new Path(recipe.id(), x, y, distance, ring, events.stream().map(pair ->
                        new OnPathEvent(container, pair.getFirst().getEventName(), pair.getFirst().alarmIcon(),
                                pair.getFirst().shipEventId(), pair.getSecond(), pair.getFirst().alertTime()))
                .sorted(Comparator.comparing(onPathEvent -> onPathEvent.startOn)).toList());
    }

    public boolean anyAlert() {
        return !getActiveAlerts().isEmpty();
    }

    public @NotNull Optional<AsteroidRecipe> getRecipe(@NotNull Level level) {
        return level.getRecipeManager().getAllRecipesFor(DrifterRecipes.ASTEROID_RECIPE_TYPE.get()).stream()
                .filter(recipe -> recipe.id().equals(recipeId)).findFirst();
    }


    public @NotNull CompoundTag saveData() {
        CompoundTag tag = new CompoundTag();

        tag.putString("recipe", recipeId().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("ring", ring);
        tag.putInt("distance", distance);
        tag.put("pathEvents", onPathEvents.stream().map(OnPathEvent::saveData).collect(Collectors.toCollection(ListTag::new)));

        return tag;
    }

    public static @Nullable Path loadData(FutureEventsContainer container, @NotNull CompoundTag tag) {
        if (!tag.contains("recipe", Tag.TAG_STRING) || !tag.contains("distance", Tag.TAG_INT)) return null;

        ResourceLocation recipeId = new ResourceLocation(tag.getString("recipe"));
        int distance = tag.getInt("distance");

        int x = tag.contains("x", Tag.TAG_INT) ? tag.getInt("x") : 0;
        int y = tag.contains("y", Tag.TAG_INT) ? tag.getInt("y") : 0;
        int ring = tag.contains("ring", Tag.TAG_INT) ? tag.getInt("ring") : 0;

        List<OnPathEvent> events;
        if (tag.contains("pathEvents", Tag.TAG_LIST)) {
            events = new ArrayList<>();
            for (Tag pathEventTag : tag.getList("pathEvents", Tag.TAG_COMPOUND)) {
                OnPathEvent onPathEvent = OnPathEvent.loadData(container, (CompoundTag) pathEventTag);
                if (onPathEvent != null) events.add(onPathEvent);
            }
        } else events = List.of();

        return new Path(recipeId, x, y, distance, ring, events);
    }


    public void startEvent(int remainDistance, Level level, ShipStructure structure) {
        Optional<OnPathEvent> any = onPathEvents.stream().filter(onPathEvent -> distance - remainDistance > onPathEvent.startOn && !onPathEvent.complete).findAny();
        any.ifPresent(onPathEvent -> {
            onPathEvent.complete = true;
            ShipEvent event = onPathEvent.getEvent();
            if (event != null) event.applyCollision(level, structure);
        });
    }

}
