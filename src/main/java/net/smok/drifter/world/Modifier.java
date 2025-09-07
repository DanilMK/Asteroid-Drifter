package net.smok.drifter.world;

import com.mojang.serialization.Codec;
import net.smok.drifter.registries.ModifierRegistries;

public interface Modifier {

    Codec<Modifier> CODEC = ModifierRegistries.MODIFIER_TYPES.byNameCodec()
            .dispatch("asteroid_modifier_type", Modifier::getType, ModifierType::codec);


    void apply(AsteroidFeature.GenerationContext context);

    ModifierType<?> getType();
}
