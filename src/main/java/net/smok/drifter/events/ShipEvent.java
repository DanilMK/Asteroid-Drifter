package net.smok.drifter.events;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.structure.ShipStructure;
import net.smok.drifter.registries.ShipEventRegistries;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ShipEvent {

    ShipEventType<?> getType();

    ResourceLocation id();

    int iconColor();

    void applyCollision(@NotNull Level level, ShipStructure structure);

    default boolean stopShip() {
        return false;
    }

    @Contract(" -> new")
    static @NotNull SavedDataSlot<Pair<ResourceLocation, ShipEvent>> createSavedData() {
        return new SavedDataSlot<>(null) {
            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains("collisionType")) {
                    ResourceLocation collisionId = new ResourceLocation(compoundTag.getString("collisionType"));
                    ShipEvent shipEvent = ShipEventRegistries.getShipEvent(collisionId);
                    if (shipEvent != null) setValue(new Pair<>(collisionId, shipEvent));
                }
            }

            @Override
            public void save(CompoundTag compoundTag) {
                if (getValue() != null) compoundTag.putString("collisionType", getValue().getFirst().toString());
            }

        };
    }
}
