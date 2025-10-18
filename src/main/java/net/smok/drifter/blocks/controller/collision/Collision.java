package net.smok.drifter.blocks.controller.collision;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.structure.ShipStructure;
import net.smok.drifter.registries.CollisionRegistries;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Collision {

    Codec<Collision> CODEC = CollisionRegistries.COLLISION_TYPES.byNameCodec()
            .dispatch("collision_type", Collision::getType, CollisionType::codec);

    CollisionType<?> getType();

    int iconColor();

    void applyCollision(@NotNull Level level, ShipStructure structure);

    @Contract(" -> new")
    static @NotNull SavedDataSlot<Pair<ResourceLocation, Collision>> createSavedData() {
        return new SavedDataSlot<>(null) {
            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains("collisionType")) {
                    ResourceLocation collisionId = new ResourceLocation(compoundTag.getString("collisionType"));
                    Collision collision = CollisionRegistries.getCollision(collisionId);
                    if (collision != null) setValue(new Pair<>(collisionId, collision));
                }
            }

            @Override
            public void save(CompoundTag compoundTag) {
                if (getValue() != null) compoundTag.putString("collisionType", getValue().getFirst().toString());
            }

            @Override
            public int get() {
                return 0;
            }

            @Override
            public void set(int value) {

            }
        };
    }
}
