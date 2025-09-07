package net.smok.drifter.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public record AsteroidFeatureConfiguration(int size, float fraction, BlockStateConfiguration filler, List<Modifier> modifiers) implements FeatureConfiguration {


    public static final Codec<AsteroidFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("size").forGetter(AsteroidFeatureConfiguration::size),
            Codec.floatRange(0, 1).optionalFieldOf("size", 0.5f).forGetter(AsteroidFeatureConfiguration::fraction),
            BlockStateConfiguration.CODEC.fieldOf("filler").forGetter(AsteroidFeatureConfiguration::filler),
            Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(AsteroidFeatureConfiguration::modifiers)

            ).apply(instance, AsteroidFeatureConfiguration::new));



    public boolean hasModifiers() {
        return !modifiers.isEmpty();
    }

}
