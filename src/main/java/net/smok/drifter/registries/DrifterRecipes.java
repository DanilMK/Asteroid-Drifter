package net.smok.drifter.registries;

import com.mojang.serialization.Codec;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipeSerializer;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.recipies.FuelRecipe;
import net.smok.drifter.recipies.MoonFarmRecipe;

import java.util.function.Function;

public final class DrifterRecipes {
    public static final ResourcefulRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_SERIALIZER, Values.MOD_ID);
    public static final ResourcefulRegistry<RecipeType<?>> RECIPE_TYPES = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_TYPE, Values.MOD_ID);



    public static final RegistryEntry<RecipeType<AsteroidRecipe>> ASTEROID_RECIPE_TYPE = registerRecipeType("asteroid");
    public static final RegistryEntry<CodecRecipeSerializer<AsteroidRecipe>> ASTEROID_RECIPE =
            registerRecipe("asteroid", ASTEROID_RECIPE_TYPE.get(), AsteroidRecipe::codec);


    public static final RegistryEntry<RecipeType<MoonFarmRecipe>> MOON_FARMLAND_TYPE = registerRecipeType("moon_farmland");
    public static final RegistryEntry<CodecRecipeSerializer<MoonFarmRecipe>> MOON_FARMLAND_RECIPE =
            registerRecipe("moon_farmland", MOON_FARMLAND_TYPE.get(), MoonFarmRecipe::codec);


    public static final RegistryEntry<RecipeType<FuelRecipe>> FUEL_TYPE = registerRecipeType("fuel");
    public static final RegistryEntry<CodecRecipeSerializer<FuelRecipe>> FUEL_RECIPE =
            registerRecipe("fuel", FUEL_TYPE.get(), FuelRecipe::codec);




    private static <T extends Recipe<?>> RegistryEntry<CodecRecipeSerializer<T>> registerRecipe(String id, RecipeType<T> recipeType, Function<ResourceLocation, Codec<T>> codec) {
        return RECIPE_SERIALIZERS.register(id, () -> new CodecRecipeSerializer<>(recipeType, codec));
    }

    private static <T extends Recipe<?>> RegistryEntry<RecipeType<T>> registerRecipeType(String id) {
        return RECIPE_TYPES.register(id, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return id;
            }
        });
    }
}
