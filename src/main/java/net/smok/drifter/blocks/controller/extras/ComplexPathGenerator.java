package net.smok.drifter.blocks.controller.extras;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.RandomSource;
import net.smok.drifter.blocks.controller.PathGenerator;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.recipies.Path;
import net.smok.drifter.recipies.PathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.smok.drifter.ShipConfig.ringsAmount;
import static net.smok.drifter.ShipConfig.distanceBetweenRingsKm;


public class ComplexPathGenerator implements PathGenerator {

    public static final int MAX_PIXELS_RANGE = 200; // max range in pixels


    @Override
    public void genAsteroidField(@NotNull ShipControllerBlockEntity controller, @NotNull RandomSource random, @NotNull List<Path> asteroids) {

        int totalRings = random.nextInt(1, ringsAmount.length);
        for (int ring = 0; ring < totalRings; ring++) {
            int segmentAmount = random.nextInt(ringsAmount[ring]) + 1;
            float segmentAngle = 1f / segmentAmount;

            for (int segment = 0; segment < segmentAmount; segment++) {
                int distance = random.nextInt((ring + 1) * (ring + 1) * distanceBetweenRingsKm, (int) ((ring + 1.5) * (ring + 1.5) * distanceBetweenRingsKm));
                double angle = (segmentAngle * segment + (random.nextFloat() * segmentAngle)) * Math.PI * 2;
                double d = (double) distance * distToPixelRatio(totalRings);
                int x = (int) (Math.cos(angle) * d);
                int y = (int) (Math.sin(angle) * d);

                AsteroidRecipe recipe = getRandomRecipe(controller.getLevel(), controller, random, distance);

                if (recipe == null) continue;
                List<Pair<PathEvent, Integer>> events = getRandomEvents(random, recipe, distance);
                asteroids.add(Path.of(controller.getFutureEventsContainer(), recipe, x, y, distance, ring, events));
             }

        }
    }


    private static double distToPixelRatio(int totalRings) {
        return (double) MAX_PIXELS_RANGE / ((totalRings + 1) * (totalRings + 1) * distanceBetweenRingsKm);
    }
}
