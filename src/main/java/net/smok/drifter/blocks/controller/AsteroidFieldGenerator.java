package net.smok.drifter.blocks.controller;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.recipies.PlacedAsteroidRecipe;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AsteroidFieldGenerator {

    void genAsteroidField(@NotNull ShipControllerBlockEntity controller, @NotNull RandomSource random, @NotNull List<PlacedAsteroidRecipe> asteroids);

    default @Nullable AsteroidRecipe getRandomRecipe(@NotNull Level level, @NotNull ShipControllerBlockEntity controller, @NotNull RandomSource random, int distance) {

        List<AsteroidRecipe> allRecipesFor1 = level.getRecipeManager().getAllRecipesFor(Values.ASTEROID_RECIPE_TYPE.get());
        List<AsteroidRecipe> filteredRecipies = allRecipesFor1
                .stream().filter(asteroidRecipe -> asteroidRecipe.matches(controller, distance)).toList();
        if (filteredRecipies.isEmpty()) return null;
        return filteredRecipies.get(random.nextInt(filteredRecipies.size()));
    }
}
