package net.smok.drifter.blocks.controller.extras;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.RandomSource;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.controller.PathGenerator;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.data.events.ShipEvent;
import net.smok.drifter.data.recipies.AsteroidRecipe;
import net.smok.drifter.data.recipies.Path;
import net.smok.drifter.utils.FlyUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ComplexPathGenerator implements PathGenerator {

    private static final int[] RINGS = new int[] {4, 8, 12, 16};
    private static final int RING_FACTOR = FlyUtils.fuelToDistance(2000); // distance between rings
    private static final int MAX_PIXELS_RANGE = 100; // max range in pixels

    private static final double DIST_TO_PIXEL_RATION = (double) MAX_PIXELS_RANGE / (RINGS.length * RING_FACTOR);


    @Override
    public void genAsteroidField(@NotNull ShipControllerBlockEntity controller, @NotNull RandomSource random, @NotNull List<Path> asteroids) {

        for (int ring = 0; ring < random.nextInt(1, RINGS.length); ring++) {
            int ringAmount = random.nextInt(RINGS[ring]) + 1;
            float hardAngle = 1f / ringAmount;

            for (int j = 0; j < ringAmount; j++) {
                int distance = random.nextInt(ring * RING_FACTOR, (ring + 1) * RING_FACTOR);
                double angle = (hardAngle * j + (random.nextFloat() * hardAngle / 2)) * Math.PI * 2;
                double d = (double) distance * DIST_TO_PIXEL_RATION;
                int x = (int) (Math.cos(angle) * d);
                int y = (int) (Math.sin(angle) * d);

                AsteroidRecipe recipe = getRandomRecipe(controller.getLevel(), controller, random, distance);

                Debug.log("Generate asteroid " + x + ":" + y + " a " + angle + " dist " + distance + " asteroid " + recipe);

                if (recipe == null) continue;
                List<Pair<ShipEvent, Integer>> events = getRandomEvents(random, recipe, distance);
                asteroids.add(Path.of(recipe, x, y, distance, events));
            }

        }
    }
}
