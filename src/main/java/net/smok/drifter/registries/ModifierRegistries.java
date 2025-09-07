package net.smok.drifter.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.world.Modifier;
import net.smok.drifter.world.ModifierType;
import net.smok.drifter.world.modifiers.NoiseModifier;
import net.smok.drifter.world.modifiers.SharSurfaceModifier;
import net.smok.drifter.world.modifiers.SmoothModifier;

public class ModifierRegistries {

    public static ResourceKey<Registry<ModifierType<?>>> MODIFIER_TYPE_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Values.MOD_ID, "worldgen/asteroid_modifier"));


    public static Registry<ModifierType<?>> MODIFIER_TYPES = new MappedRegistry<>(MODIFIER_TYPE_KEY, Lifecycle.stable());


    public static final ModifierType<SharSurfaceModifier> SHARP_SURFACE = register("sharp_surface", SharSurfaceModifier.CODEC);
    public static final ModifierType<SmoothModifier> SMOOTH_SURFACE = register("smooth_surface", SmoothModifier.CODEC);
    public static final ModifierType<NoiseModifier> NOISE = register("noise_replace", NoiseModifier.CODEC);


    private static <M extends Modifier> ModifierType<M> register(String name, Codec<M> codec) {
        return Registry.register(MODIFIER_TYPES, new ResourceLocation(Values.MOD_ID, name), () -> codec);
    }

    public static void init() {}
}
