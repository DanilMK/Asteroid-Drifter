package net.smok.drifter.widgets;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

public class GuiUtils {


    /**
     * Draw circle, with 1px arc thickness
     *
     * @param consumer Any VertexConsumer, or BufBuilder
     * @param x        Center X position on screen
     * @param y        Center Y position on screen
     * @param radius   Radius of circle in pixels
     * @param sides    segments amount
     * @param color    color of circle
     */
    public static void drawCircle(VertexConsumer consumer, double x, double y, double radius, int sides, int color) {
        drawCircle(consumer, x, y, radius, sides, color, 0.5);
    }

    /**
     * Draw circle
     *
     * @param consumer  Any VertexConsumer, or BufBuilder
     * @param x         Center X position on screen
     * @param y         Center Y position on screen
     * @param radius    Radius of circle in pixels
     * @param sides     Amount of sides. More side more smooth
     * @param color     Color of circle
     * @param thickness Line thickness in half of pixels, i.e. 0.5 = 1px
     */
    public static void drawCircle(VertexConsumer consumer, double x, double y, double radius, int sides,
                                  int color, double thickness) {
        for(double r = radius - thickness; r <= radius + thickness; r += 0.1) {
            for(int i = 0; i < sides; ++i) {
                double angle = (double)i * 2.0 * Math.PI / (double)sides;
                double nextAngle = (double)(i + 1) * 2.0 * Math.PI / (double)sides;
                double x1 = x + r * Math.cos(angle);
                double y1 = y + r * Math.sin(angle);
                double x2 = x + r * Math.cos(nextAngle);
                double y2 = y + r * Math.sin(nextAngle);
                consumer.vertex(x1, y1, 0.0).color(color).endVertex();
                consumer.vertex(x2, y2, 0.0).color(color).endVertex();
            }
        }
    }


    /**
     * Draw circle arc start from right position clockwise with serifs,
     * with 1px arc thickness, 0.1px serif thickness, 8px serif size
     *
     * @param consumer    Any VertexConsumer, or BufBuilder
     * @param x           Center X position on screen
     * @param y           Center Y position on screen
     * @param radius      Radius of circle in pixels
     * @param sides       Amount of sides. More side more smooth
     * @param mainColor   Main segment color
     * @param secondColor Second segment color
     * @param start       Start of segment. Between 0 and 1
     * @param end         End of segment. Between 0 and 1
     * @param amount      Main color segment size. Between 0 and 1 - from start to end segment
     */
    public static void drawSerifArc(VertexConsumer consumer, double x, double y, double radius, int sides,
                                    int mainColor, int secondColor, double start, double end, double amount) {
        drawArc(consumer, x, y, radius, sides, mainColor, secondColor, start, end, amount, 0.5, 0.05, 4);
    }

    /**
     * Draw circle arc start from right position clockwise with serifs
     *
     * @param consumer       Any VertexConsumer, or BufBuilder
     * @param x              Center X position on screen
     * @param y              Center Y position on screen
     * @param radius         Radius of circle in pixels
     * @param sides          Amount of sides. More side more smooth
     * @param mainColor      Main segment color
     * @param secondColor    Second segment color
     * @param start          Start of segment. Between 0 and 1
     * @param end            End of segment. Between 0 and 1
     * @param amount         Main color segment size. Between 0 and 1 - from start to end segment
     * @param arcThickness   Arc line thickness in half of pixels, i.e. 0.5 = 1px
     * @param serifThickness Serif line thickness in half of pixels, i.e. 0.5 = 1px
     * @param serifSize      Serif size in half of pixels, i.e. 0.5 = 1px
     */
    public static void drawArc(VertexConsumer consumer, double x, double y, double radius, int sides, int mainColor,
                               int secondColor, double start, double end, double amount, double arcThickness,
                               double serifThickness, double serifSize) {

        int startSegment = (int) (start * sides);
        int endSegment = (int) (end * sides);
        int mid = (int) (amount * (endSegment - startSegment)) + startSegment;

        for(double r = radius - arcThickness; r <= radius + arcThickness; r += 0.1) {
            for(int i = startSegment; i < sides & i < endSegment; ++i) {

                Pair<Double, Double> first = pointOnCircle(i, sides, r, x, y);
                Pair<Double, Double> second = pointOnCircle(i + 1, sides, r, x, y);

                int color1 = i < mid ? mainColor : secondColor;
                consumer.vertex(first.getA(), first.getB(), 0.0).color(color1).endVertex();
                consumer.vertex(second.getA(), second.getB(), 0.0).color(color1).endVertex();
            }
        }

        for (double t = -serifThickness; t <= serifThickness; t += 0.01) {
            // add serif on start
            {
                Pair<Double, Double> first = pointOnCircle(startSegment + t, sides, radius - serifSize, x, y);
                Pair<Double, Double> second = pointOnCircle(startSegment + t, sides, radius + serifSize, x, y);

                consumer.vertex(first.getA(), first.getB(), 0.0).color(mainColor).endVertex();
                consumer.vertex(second.getA(), second.getB(), 0.0).color(mainColor).endVertex();
            }
            // add serif on mid
            {
                Pair<Double, Double> first = pointOnCircle(endSegment + t, sides, radius - serifSize, x, y);
                Pair<Double, Double> second = pointOnCircle(endSegment + t, sides, radius + serifSize, x, y);

                consumer.vertex(first.getA(), first.getB(), 0.0).color(secondColor).endVertex();
                consumer.vertex(second.getA(), second.getB(), 0.0).color(secondColor).endVertex();
            }
            // add serif on end
            {
                Pair<Double, Double> first = pointOnCircle(mid + t, sides, radius - serifSize, x, y);
                Pair<Double, Double> second = pointOnCircle(mid + t, sides, radius + serifSize, x, y);

                consumer.vertex(first.getA(), first.getB(), 0.0).color(mainColor).endVertex();
                consumer.vertex(second.getA(), second.getB(), 0.0).color(mainColor).endVertex();
            }
        }

    }

    /**
     * Draw circle arc start from right position clockwise with 1px arc thickness
     *
     * @param vertexConsumer Any VertexConsumer, or BufBuilder
     * @param x              Center X position on screen
     * @param y              Center Y position on screen
     * @param radius         Radius of circle in pixels
     * @param sides          Amount of sides. More side more smooth
     * @param mainColor      Main segment color
     * @param secondColor    Second segment color
     * @param start          Start of segment. Between 0 and 1
     * @param end            End of segment. Between 0 and 1
     * @param amount         Main color segment size. Between 0 and 1 - from start to end segment
     */
    public static void drawArc(VertexConsumer vertexConsumer, double x, double y, double radius, int sides, int mainColor, int secondColor, double start, double end, double amount) {
        drawSerifArc(vertexConsumer, x, y, radius, sides, mainColor, secondColor, start, end, amount, 0.5);
    }

    /**
     * Draw circle arc start from right position clockwise
     *
     * @param consumer     Any VertexConsumer, or BufBuilder
     * @param x            Center X position on screen
     * @param y            Center Y position on screen
     * @param radius       Radius of circle in pixels
     * @param sides        Amount of sides. More side more smooth
     * @param mainColor    Main segment color
     * @param secondColor  Second segment color
     * @param start        Start of segment. Between 0 and 1
     * @param end          End of segment. Between 0 and 1
     * @param amount       Main color segment size. Between 0 and 1 - from start to end segment
     * @param arcThickness Arc line thickness in half of pixels, i.e. 0.5 = 1px
     */
    public static void drawSerifArc(VertexConsumer consumer, double x, double y, double radius, int sides, int mainColor, int secondColor, double start, double end, double amount, double arcThickness) {

        int startSegment = (int) (start * sides);
        int endSegment = (int) (end * sides);
        int mid = (int) (amount * (endSegment - startSegment)) + startSegment;

        for(double r = radius - arcThickness; r <= radius + arcThickness; r += 0.1) {
            for(int i = startSegment; i < sides & i < endSegment; ++i) {

                Pair<Double, Double> first = pointOnCircle(i, sides, r, x, y);
                Pair<Double, Double> second = pointOnCircle(i + 1, sides, r, x, y);

                int color1 = i < mid ? mainColor : secondColor;
                consumer.vertex(first.getA(), first.getB(), 0.0).color(color1).endVertex();
                consumer.vertex(second.getA(), second.getB(), 0.0).color(color1).endVertex();
            }
        }
    }



    /**
     * Check if point in circle
     */
    public static boolean isInCircle(double circleX, double circleY, double radius, float x, float y) {
        double px = x - circleX;
        double py = y - circleY;
        double dist = px * px + py * py;
        return dist < radius * radius;
    }


    /**
     * Find point on circle by side
     */
    @Contract("_, _, _, _, _ -> new")
    public static @NotNull Pair<Double, Double> pointOnCircle(double side, double sides, double radius, double x, double y) {
        double angle = side * 2.0 * Math.PI / sides;
        double x1 = x + radius * Math.cos(angle);
        double y1 = y + radius * Math.sin(angle);

        return new Pair<>(x1, y1);
    }

    public static void renderDashedPathLine(VertexConsumer bufferBuilder, float startX, float startY, float endX, float endY, float dash, int color, float cycleTime) {
        double dx = endX - startX;
        double dy = endY - startY;
        double r = Math.sqrt(dx * dx + dy * dy);

        double normalX = dx * dash / r;
        double normalY = dy * dash / r;

        double shift = getTime() % cycleTime / cycleTime;

        double px = startX + shift * normalX * 2 - normalX * 0.75;
        double py = startY + shift * normalY * 2 - normalY * 0.75;



        bufferBuilder.vertex(startX, startY, 0.0).color(color).endVertex();
        int i = 0;
        for (double p = 0; p < r - dash * 2; p += dash) {
            px += normalX;
            py += normalY;
            bufferBuilder.vertex(px, py, 0.0).color(color).endVertex();
            i++;
        }
        if (i % 2 == 0) bufferBuilder.vertex(endX, endY, 0.0).color(color).endVertex();
    }

    public static float getTime() {
        return Util.getMillis() / 100.0F;
    }
}
