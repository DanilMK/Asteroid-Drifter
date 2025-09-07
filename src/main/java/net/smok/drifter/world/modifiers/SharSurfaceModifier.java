package net.smok.drifter.world.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.smok.drifter.registries.ModifierRegistries;
import net.smok.drifter.utils.WorldGenUtils;
import net.smok.drifter.world.AsteroidFeature;
import net.smok.drifter.world.Modifier;
import net.smok.drifter.world.ModifierType;

// Sharpness is integer between 0 and 100
public record SharSurfaceModifier(int minSharpness, int maxSharpness) implements Modifier {

    public static final Codec<SharSurfaceModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 100).fieldOf("min_sharpness").forGetter(SharSurfaceModifier::minSharpness),
            Codec.intRange(0, 100).fieldOf("max_sharpness").forGetter(SharSurfaceModifier::minSharpness)
    ).apply(instance, SharSurfaceModifier::new));

    @Override
    public void apply(AsteroidFeature.GenerationContext context) {
        WorldGenUtils.sharpSurface(context.blocks(), context.randomSource(), context.randomSource().nextInt(minSharpness, maxSharpness) / 100f);
    }

    @Override
    public ModifierType<?> getType() {
        return ModifierRegistries.SHARP_SURFACE;
    }
}
