package net.smok.drifter.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record MetaBalls(Ball[] balls) implements NoiseMap {

    public double apply(BlockPos pos) {
        return sumDensity(balls, pos);
    }

    @Override
    public String toString() {
        return "MetaBalls{" +
                "balls=" + Arrays.toString(balls) +
                '}';
    }


    public static MetaBalls createBallsNoise(RandomSource random, BlockPos origin, int size, float fraction) {
        int ballDiameter = Math.max(1, (int) (size * fraction));

        List<Ball> balls = new ArrayList<>();

        for (int x = 0; x < size; x+= ballDiameter) {
            for (int y = 0; y < size; y+= ballDiameter) {
                for (int z = 0; z < size; z+= ballDiameter) {
                    int radius = random.nextInt(Math.max(1, ballDiameter / 4), ballDiameter / 2);

                    BlockPos pos = BlockPos.randomInCube(random, 1, origin.offset(x, y, z), ballDiameter / 2)
                            .iterator().next();

                    balls.add(new Ball(radius, pos));
                }
            }
        }

        return new MetaBalls(balls.toArray(Ball[]::new));
    }

    public static MetaBalls generateDots(BlockPos origin, int minAmount, double fraction, double spread, int maxRadius, RandomSource random) {

        int amount = (int) (minAmount + fraction * maxRadius);
        if (amount == 0) return new MetaBalls(new Ball[0]);

        int minSize = maxRadius / amount / 2;
        int maxSize = maxRadius / ((amount - 1) / 2 + 1);

        Ball[] balls = new Ball[amount];
        for (int i = 0; i < amount; i++) {

            int radius = random.nextInt(minSize, maxSize);
            int shift = (int) (radius + maxRadius * spread);
            if (shift <= 0) {
                balls[i] = new Ball(radius, origin);
                continue;
            }
            int x = random.nextInt(-shift, shift);
            int y = random.nextInt(-shift, shift);
            int z = random.nextInt(-shift, shift);
            balls[i] = new Ball(radius, origin.offset(x, y, z));
        }
        return new MetaBalls(balls);
    }

    public static double sumDensity(Ball[] balls, BlockPos pos) {
        return Arrays.stream(balls).mapToDouble(ball -> ball.apply(pos)).sum() / 2;
    }



    public record Ball(int radius, BlockPos position) {
        public double apply(BlockPos pos) {
            return radius * radius / position.distSqr(pos);
        }
    }
}
