package net.smok.drifter.blocks.controller.extras;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.controller.AsteroidFieldGenerator;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.recipies.PlacedAsteroidRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SimpleAsteroidFieldGenerator implements AsteroidFieldGenerator {

    private static final int[] startX = new int[] {-88, -8, +72, -88, +92, -88, -8, +72};
    private static final int[] startY = new int[] {-88, -88, -88, -8, -8, +72, +72, +72};
    private static final int ASTEROID_SCATTER = 30;
    private static final int DISTANCE_FACTOR = 1000; // convert UI distance to km



    @Override
    public void genAsteroidField(@NotNull ShipControllerBlockEntity controller, @NotNull RandomSource random, @NotNull List<PlacedAsteroidRecipe> asteroids) {
        for (int i = 0; i < 8; i++) {
            generatePlacedAsteroid(asteroids, i, random, controller.getLevel(), controller);
        }
    }


    private void generatePlacedAsteroid(List<PlacedAsteroidRecipe> asteroids, int i, @NotNull RandomSource random, Level level, ShipControllerBlockEntity controller) {

        int xDist = startX[i] + random.nextInt(-ASTEROID_SCATTER, +ASTEROID_SCATTER);
        int yDist = startY[i] + random.nextInt(-ASTEROID_SCATTER, +ASTEROID_SCATTER);
        int distance = (xDist * xDist + yDist * yDist) * DISTANCE_FACTOR;
        AsteroidRecipe recipe = getRandomRecipe(level, controller, random, distance);
        if (recipe == null) return;

        asteroids.set(i, new PlacedAsteroidRecipe(recipe.id(), xDist, yDist, distance));
    }
}
