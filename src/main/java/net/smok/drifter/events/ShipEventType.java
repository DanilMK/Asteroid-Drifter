package net.smok.drifter.events;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public interface ShipEventType<C extends ShipEvent> {

    Codec<C> codec(ResourceLocation id);
}
