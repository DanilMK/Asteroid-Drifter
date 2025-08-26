package net.smok.drifter.widgets;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

public class CircleDrawer {


    public static void drawSegment(BufferBuilder bufferBuilder, double x, double y, double radius, int sides, int color) {
        for(double r = radius - 0.5; r <= radius + 0.5; r += 0.1) {
            for(int i = 0; i < sides; ++i) {
                double angle = (double)i * 2.0 * Math.PI / (double)sides;
                double nextAngle = (double)(i + 1) * 2.0 * Math.PI / (double)sides;
                double x1 = x + r * Math.cos(angle);
                double y1 = y + r * Math.sin(angle);
                double x2 = x + r * Math.cos(nextAngle);
                double y2 = y + r * Math.sin(nextAngle);
                bufferBuilder.vertex(x1, y1, 0.0).color(color).endVertex();
                bufferBuilder.vertex(x2, y2, 0.0).color(color).endVertex();
            }
        }
    }

    public static void drawSegment(BufferBuilder bufferBuilder, double x, double y, double radius, int sides, int color, int color2, double segmentStart, double segmentEnd) {
        for(double r = radius - 0.5; r <= radius + 0.5; r += 0.1) {
            for(int i = 0; i < sides; ++i) {
                double angle = (double)i * 2.0 * Math.PI / (double)sides;
                double nextAngle = (double)(i + 1) * 2.0 * Math.PI / (double)sides;
                double x1 = x + r * Math.cos(angle);
                double y1 = y + r * Math.sin(angle);
                double x2 = x + r * Math.cos(nextAngle);
                double y2 = y + r * Math.sin(nextAngle);
                int color1 = i > segmentStart & i < segmentEnd ? color : color2;
                bufferBuilder.vertex(x1, y1, 0.0).color(color1).endVertex();
                bufferBuilder.vertex(x2, y2, 0.0).color(color1).endVertex();
            }
        }
    }

    /**
     * Draw circle segment start from right position clockwise
     * @param bufferBuilder Builder
     * @param x X position on screen
     * @param y Y position on screen
     * @param radius Radius of circle in pixels
     * @param sides Amount of sides. More side more smooth
     * @param mainColor Main segment color
     * @param secondColor Second segment color
     * @param start Start of segment. Between 0 and 1
     * @param end End of segment. Between 0 and 1
     * @param amount Amount of main color. Between 0 and 1 - from start to end segment
     */
    public static void drawSegment(BufferBuilder bufferBuilder, double x, double y, double radius, int sides, int mainColor, int secondColor, double start, double end, double amount) {

        int startSegment = (int) (start * sides);
        int endSegment = (int) (end * sides);
        int mid = (int) (amount * (endSegment - startSegment)) + startSegment;

        for(double r = radius - 0.5; r <= radius + 0.5; r += 0.1) {
            for(int i = startSegment; i < sides & i < endSegment; ++i) {

                Pair<Double, Double> first = pointOnCircle(i, sides, r, x, y);
                Pair<Double, Double> second = pointOnCircle(i + 1, sides, r, x, y);

                int color1 = i < mid ? mainColor : secondColor;
                bufferBuilder.vertex(first.getA(), first.getB(), 0.0).color(color1).endVertex();
                bufferBuilder.vertex(second.getA(), second.getB(), 0.0).color(color1).endVertex();
            }
        }

        // add serif on start, mid and end
        for (double t = -0.05; t <= 0.05; t += 0.01) {
            {
                Pair<Double, Double> first = pointOnCircle(startSegment + t, sides, radius - 4, x, y);
                Pair<Double, Double> second = pointOnCircle(startSegment + t, sides, radius + 4, x, y);

                bufferBuilder.vertex(first.getA(), first.getB(), 0.0).color(mainColor).endVertex();
                bufferBuilder.vertex(second.getA(), second.getB(), 0.0).color(mainColor).endVertex();
            }
            {
                Pair<Double, Double> first = pointOnCircle(endSegment + t, sides, radius - 4, x, y);
                Pair<Double, Double> second = pointOnCircle(endSegment + t, sides, radius + 4, x, y);

                bufferBuilder.vertex(first.getA(), first.getB(), 0.0).color(secondColor).endVertex();
                bufferBuilder.vertex(second.getA(), second.getB(), 0.0).color(secondColor).endVertex();
            }
            {
                Pair<Double, Double> first = pointOnCircle(mid + t, sides, radius - 4, x, y);
                Pair<Double, Double> second = pointOnCircle(mid + t, sides, radius + 4, x, y);

                bufferBuilder.vertex(first.getA(), first.getB(), 0.0).color(mainColor).endVertex();
                bufferBuilder.vertex(second.getA(), second.getB(), 0.0).color(mainColor).endVertex();
            }
        }

    }


    public static void drawCircleFilled(BufferBuilder bufferBuilder, double x, double y, double radius, int sides, int edgeMain, int edgeSecond, int fillMain, int fillSecond, double amount) {

        double yFix = y + amount * radius * 2 - radius;
        for(double r = radius - 0.5; r <= radius + 0.5; r += 0.1) {

            for(int i = 0; i < sides; ++i) {
                double angle = (double)i * 2.0 * Math.PI / (double)sides;
                double nextAngle = (double)(i + 1) * 2.0 * Math.PI / (double)sides;
                double x1 = x + r * Math.sin(angle);
                double y1 = y + r * Math.cos(angle);
                double x2 = x + r * Math.sin(nextAngle);
                double y2 = y + r * Math.cos(nextAngle);
                int edge = y1 < yFix ? edgeMain : edgeSecond;
                bufferBuilder.vertex(x1, y1, 0.0).color(edge).endVertex();
                bufferBuilder.vertex(x2, y2, 0.0).color(edge).endVertex();
            }

            for (int i = 0; i < sides / 2; i++) {
                double angle = (double)i * 2.0 * Math.PI / (double)sides;
                double nextAngle = (double)(sides - i) * 2.0 * Math.PI / (double)sides;
                double x1 = x + r * Math.sin(angle);
                double y1 = y + r * Math.cos(angle);
                double x2 = x + r * Math.sin(nextAngle);
                double y2 = y + r * Math.cos(nextAngle);
                int fill = y1 < yFix ? fillMain : fillSecond;
                bufferBuilder.vertex(x1, y1, 0.0).color(fill).endVertex();
                bufferBuilder.vertex(x2, y2, 0.0).color(fill).endVertex();
            }

        }
    }
    /**
     * Draw circle segment start from right position counterclockwise
     * @param bufferBuilder Builder
     * @param x X position on screen
     * @param y Y position on screen
     * @param radius Radius of circle in pixels
     * @param sides Amount of sides. More side more smooth
     * @param color Main segment color
     * @param color2 Second segment color
     * @param start Start of segment. Between 0 and 1
     * @param end End of segment. Between 0 and 1
     * @param amount Amount of main color. Between 0 and 1 - from start to end segment
     */
    public static void drawSegmentCounter(BufferBuilder bufferBuilder, double x, double y, double radius, int sides, int color, int color2, double start, double end, double amount) {

        int startSegment = (int) (start * sides);
        int endSegment = (int) (end * sides);
        int a = (int) (amount * (endSegment - startSegment));

        for(double r = radius - 0.5; r <= radius + 0.5; r += 0.1) {
            for(int i = endSegment; i > startSegment & i > 0; --i) {
                double angle = (double)i * 2.0 * Math.PI / (double)sides;
                double nextAngle = (double)(i + 1) * 2.0 * Math.PI / (double)sides;
                double x1 = x + r * Math.cos(angle);
                double y1 = y + r * Math.sin(angle);
                double x2 = x + r * Math.cos(nextAngle);
                double y2 = y + r * Math.sin(nextAngle);
                int color1 = i > a ? color : color2;
                bufferBuilder.vertex(x1, y1, 0.0).color(color1).endVertex();
                bufferBuilder.vertex(x2, y2, 0.0).color(color1).endVertex();
            }
        }
    }

    /**
     * Check if point in circle
     * @param circleX X pos of circle
     * @param circleY Y pos of circle
     * @param radius circle radius
     * @param x X pos of point
     * @param y Y pos of point
     * @return point in circle
     */
    public static boolean isInCircle(double circleX, double circleY, double radius, float x, float y) {
        double px = x - circleX;
        double py = y - circleY;
        double dist = px * px + py * py;
        return dist < radius * radius;
    }

    /**
     *
     * @return Return linear interpolation
     */
    public static double lerp(double a, double a0, double a1, double b0, double b1) {
        return b0 + (a - a0) * (b1 - b0) / (a1 - a0);
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull Pair<Double, Double> pointOnCircle(double side, double sides, double radius, double x, double y) {
        double angle = side * 2.0 * Math.PI / sides;
        double x1 = x + radius * Math.cos(angle);
        double y1 = y + radius * Math.sin(angle);

        return new Pair<>(x1, y1);
    }
}
