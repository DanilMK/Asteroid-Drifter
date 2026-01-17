package net.smok.drifter.blocks.controller.extras;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.ShipConfig;
import net.smok.drifter.blocks.alert.Alert;
import net.smok.drifter.blocks.alert.AlertSound;
import net.smok.drifter.blocks.alert.Icon;
import net.smok.drifter.events.ShipEvent;
import net.smok.drifter.recipies.PathEvent;
import net.smok.drifter.registries.ShipEventRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FutureShipEvent extends Alert {

    private final FutureEventsContainer container;
    private final ShipEvent shipEvent;
    private int ticks;

    @Contract("_, _ -> new")
    public static @Nullable FutureShipEvent fromPathEvent(FutureEventsContainer container, @NotNull PathEvent pathEvent) {
        if (pathEvent.getShipEvent() == null) return null;
        return new FutureShipEvent(container, pathEvent.alarmIcon(), pathEvent.getShipEvent(), (int) (pathEvent.alertTime() * 20f));
    }

    public FutureShipEvent(FutureEventsContainer container, @NotNull ShipEvent shipEvent, int ticks) {
        super(container, shipEvent.id().toLanguageKey("ship_event"));
        this.container = container;
        this.shipEvent = shipEvent;
        this.ticks = ticks;
        this.active = true;
    }

    public FutureShipEvent(FutureEventsContainer container, Icon defaultIcon, @NotNull ShipEvent shipEvent, int ticks) {
        super(container, defaultIcon, shipEvent.id().toLanguageKey("ship_event"));
        this.container = container;
        this.shipEvent = shipEvent;
        this.ticks = ticks;
        this.active = true;
    }

    @Override
    public @NotNull Component subText() {
        return Component.literal(ShipConfig.timeToString(ticks / 120f));
    }

    @Override
    public void setSound(AlertSound sound) {
        container.setSound(sound);
    }

    @Override
    public AlertSound getSound() {
        return container.getCommonSound();
    }

    @Override
    public boolean canEditIcon() {
        return false;
    }

    @Override
    public boolean canEditName() {
        return false;
    }

    public boolean tick() {
        ticks--;
        if (ticks <= 0) {
            container.applyEvent(shipEvent);
            return true;
        }
        return false;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("event", shipEvent.id().toString());
        tag.putInt("ticks", ticks);
        icon.save(tag);
        return tag;
    }

    public static @Nullable FutureShipEvent load(@NotNull FutureEventsContainer container, @NotNull CompoundTag tag) {
        if (tag.contains("event", Tag.TAG_STRING) && tag.contains("ticks", Tag.TAG_INT)) {
            ResourceLocation eventId = new ResourceLocation(tag.getString("event"));
            ShipEvent shipEvent = ShipEventRegistries.getShipEvent(eventId);
            if (shipEvent != null) {
                int ticks = tag.getInt("ticks");
                FutureShipEvent futureShipEvent = new FutureShipEvent(container, shipEvent, ticks);
                futureShipEvent.icon.load(tag);
                return futureShipEvent;
            }
        }
        return null;
    }
}
