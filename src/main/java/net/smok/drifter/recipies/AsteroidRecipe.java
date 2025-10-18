package net.smok.drifter.recipies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.common.codecs.recipes.ItemStackCodec;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record AsteroidRecipe(ResourceLocation id, ItemStack icon, List<String> tooltips, int size,
                             Optional<ResourceLocation> structure, Optional<ResourceLocation> feature, int minDistance,
                             int maxDistance) implements CodecRecipe<Container> {



    @Override
    public @NotNull ResourceLocation id() {
        return id;
    }

    @Override @Deprecated
    public boolean matches(Container container, Level level) {
        return true;
    }

    public boolean matches(ShipControllerBlockEntity controllerBlock, int distance) {
        return distance > minDistance & distance < maxDistance;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Values.ASTEROID_RECIPE.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Values.ASTEROID_RECIPE_TYPE.get();
    }

    public static Codec<AsteroidRecipe> codec(ResourceLocation id) {
        return RecordCodecBuilder.create(instance -> instance.group(
                RecordCodecBuilder.point(id),
                ItemStackCodec.CODEC.fieldOf("icon").forGetter(AsteroidRecipe::icon),
                Codec.STRING.listOf().fieldOf("tooltips").forGetter(AsteroidRecipe::tooltips),
                Codec.INT.fieldOf("size").forGetter(AsteroidRecipe::size),
                ResourceLocation.CODEC.optionalFieldOf("structure").forGetter(AsteroidRecipe::structure),
                ResourceLocation.CODEC.optionalFieldOf("configured_feature").forGetter(AsteroidRecipe::feature),
                Codec.INT.optionalFieldOf("min_distance", 0).forGetter(AsteroidRecipe::minDistance),
                Codec.INT.optionalFieldOf("min_distance", Integer.MAX_VALUE).forGetter(AsteroidRecipe::maxDistance)
        ).apply(instance, AsteroidRecipe::new));
    }
}
