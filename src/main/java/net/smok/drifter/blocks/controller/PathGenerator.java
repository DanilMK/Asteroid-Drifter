package net.smok.drifter.blocks.controller;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.smok.drifter.events.ShipEvent;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.recipies.Path;
import net.smok.drifter.recipies.PathEvent;
import net.smok.drifter.registries.DrifterRecipes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface PathGenerator {

    void genAsteroidField(@NotNull ShipControllerBlockEntity controller, @NotNull RandomSource random, @NotNull List<Path> asteroids);

    default @Nullable AsteroidRecipe getRandomRecipe(@NotNull Level level, @NotNull ShipControllerBlockEntity controller, @NotNull RandomSource random, int distance) {

        List<AsteroidRecipe> filteredRecipies = level.getRecipeManager().getAllRecipesFor(DrifterRecipes.ASTEROID_RECIPE_TYPE.get())
                .stream().filter(asteroidRecipe -> asteroidRecipe.matches(controller, distance)).toList();
        if (filteredRecipies.isEmpty()) return null;
        return filteredRecipies.get(random.nextInt(filteredRecipies.size()));
    }

    @NotNull
    default List<Pair<ShipEvent, Integer>> getRandomEvents(@NotNull RandomSource random, AsteroidRecipe recipe, int distance) {
        List<Pair<ShipEvent, Integer>> events = new ArrayList<>();
        for (PathEvent pathEvent : recipe.pathEvents()) {
            if (pathEvent.chance() > random.nextFloat()) continue;
            int dist = random.nextInt((int) (pathEvent.minPathTraveled() * distance), (int) (pathEvent.maxPathTraveled() * distance));
            events.add(new Pair<>(pathEvent.getShipEvent(), dist));
        }
        return events;
    }
}
