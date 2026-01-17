package net.smok.drifter.blocks.controller.extras;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.alert.Alert;
import net.smok.drifter.blocks.alert.AlertSound;
import net.smok.drifter.blocks.alert.Detector;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.blocks.structure.ShipStructure;
import net.smok.drifter.events.ShipEvent;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FutureEventsContainer implements Detector {

    private final ShipControllerBlockEntity controller;

    private final SavedDataSlot<AlertSound> commonSound =
            AlertSound.savedDataSlot(AlertSound.SOUND_PRESETS[0], "common_alert_sound");
    private final ArrayList<FutureShipEvent> shipEvents = new ArrayList<>();


    public FutureEventsContainer(ShipControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public List<Alert> getAllAlerts() {
        return shipEvents.stream().map(futureShipEvent -> (Alert) futureShipEvent).toList();
    }

    @Override
    public void setChanged() {
        controller.setChanged();
    }

    @Override
    public BlockPos getBlockPos() {
        return controller.getBlockPos();
    }

    public void setSound(@NotNull AlertSound sound) {
        if (commonSound.getValue().equals(sound)) return;
        commonSound.setValue(sound);
        setChanged();
    }

    public AlertSound getCommonSound() {
        return commonSound.getValue();
    }

    public void tick() {
        shipEvents.removeIf(FutureShipEvent::tick);
    }

    public void applyEvent(ShipEvent event) {
        Level level = controller.getLevel();
        if (level == null) {
            Debug.warn("Event cannot be invoked out of World Level. Event id: " + event.id() +
                    ", Controller position: " + controller.getBlockPos());
            return;
        }

        ShipStructure.findStructure(level, controller.getBlockPos()).ifPresentOrElse(
                structure -> event.applyCollision(level, structure),
                () -> Debug.warn("Event cannot be invoked without Ship Structure. Event id: " +
                        event.id() + ", Controller position: " + controller.getBlockPos()));
    }

    public void save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        shipEvents.forEach(futureShipEvent -> list.add(futureShipEvent.save()));
        tag.put("future_events", list);
        commonSound.save(tag);
    }

    public void load(@NotNull CompoundTag tag) {
        commonSound.load(tag);
        if (tag.contains("future_events", Tag.TAG_LIST)) {
            tag.getList("future_events", Tag.TAG_COMPOUND).forEach(t -> FutureShipEvent.load(this, tag));
        }
    }

    public float getTotalDistance() {
        return controller.getTotalDistance();
    }

    public float maxSpeed() {
        return controller.maxSpeed();
    }

    public float getRemainDistance() {
        return controller.getRemainDistance();
    }
}
