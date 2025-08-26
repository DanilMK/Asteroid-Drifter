package net.smok.drifter.recipies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.common.codecs.recipes.ItemStackCodec;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;

public record AsteroidRecipe(ResourceLocation id, ItemStack icon, ResourceLocation structure) implements CodecRecipe<ShipControllerBlockEntity> {



    @Override
    public @NotNull ResourceLocation id() {
        return id;
    }

    @Override
    public boolean matches(ShipControllerBlockEntity container, Level level) {
        return true;
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
        return RecordCodecBuilder.create(instance -> instance.group(RecordCodecBuilder.point(id),
                ItemStackCodec.CODEC.fieldOf("icon").forGetter(AsteroidRecipe::icon),
                ResourceLocation.CODEC.fieldOf("structure").forGetter(AsteroidRecipe::structure)).apply(instance, AsteroidRecipe::new));
    }
}
