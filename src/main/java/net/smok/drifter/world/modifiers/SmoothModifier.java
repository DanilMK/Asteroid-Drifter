package net.smok.drifter.world.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.smok.drifter.registries.ModifierRegistries;
import net.smok.drifter.utils.WorldGenUtils;
import net.smok.drifter.world.AsteroidFeature;
import net.smok.drifter.world.Modifier;
import net.smok.drifter.world.ModifierType;

public record SmoothModifier(int smooth) implements Modifier {

    public static final Codec<SmoothModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("smooth").forGetter(SmoothModifier::smooth)
    ).apply(instance, SmoothModifier::new));


    @Override
    public void apply(AsteroidFeature.GenerationContext context) {
        WorldGenUtils.smooth(smooth, context.blocks());
    }

    @Override
    public ModifierType<?> getType() {
        return ModifierRegistries.SMOOTH_SURFACE;
    }
}
