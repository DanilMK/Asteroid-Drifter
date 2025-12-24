package net.smok.drifter;

import com.teamresourceful.resourcefulconfig.common.annotations.Category;
import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Category(
        id = "ShipConfig",
        translation = "config.drifter.ship_config"
)
public final class ShipConfig {


    public void init() {
        if (kmFormatSeparator.length() != 2) kmFormatSeparator = " ,";
        DecimalFormatSymbols symbols = kmFormat.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(kmFormatSeparator.charAt(0));
        symbols.setDecimalSeparator(kmFormatSeparator.charAt(1));

        kmFormat.applyPattern(kmFormatPattern);
        kmFormat.setDecimalFormatSymbols(symbols);

    }

    @ConfigEntry(
            id = "ringAmount",
            type = EntryType.INTEGER,
            translation = "config.drifter.rings_amount"
    )
    @Comment(value = "Paths in each ring", translation = "config.drifter.rings_amount.comment")
    public static int[] ringsAmount = new int[] {2, 4, 8, 12};

    @ConfigEntry(
            id = "distanceBetweenRingsMinutes",
            type = EntryType.FLOAT,
            translation = "config.drifter.distance_between_rings_minutes"
    )
    @Comment(value = "Average time require to get the ring.", translation = "config.drifter.distance_between_rings_minutes.comment")
    public static float distanceBetweenRingsMinutes = 15f;

    @ConfigEntry(
            id = "distanceBetweenRingsKm",
            type = EntryType.INTEGER,
            translation = "config.drifter.distance_between_rings_km"
    )
    @Comment(value = "Distance between rings in kilometers.", translation = "config.drifter.distance_between_rings_km.comment")
    public static int distanceBetweenRingsKm = 1000000;

    @ConfigEntry(
            id = "accelerationTime",
            type = EntryType.FLOAT,
            translation = "config.drifter.acceleration_time"
    )
    @Comment(value = "Time to get max speed in minutes.", translation = "config.drifter.acceleration_time.comment")
    public static float accelerationTime = 0.25f;

    @ConfigEntry(
            id = "brakingTime",
            type = EntryType.FLOAT,
            translation = "config.drifter.braking_time"
    )
    @Comment(value = "Time to stop in minutes.", translation = "config.drifter.braking_time.comment")
    public static float brakingTime = 0.5f;

    @ConfigEntry(
            id = "kmFormatPattern",
            type = EntryType.STRING,
            translation = "config.drifter.km_format_pattern"
    )
    @Comment(value = "Format of distance describe in all places.", translation = "config.drifter.km_format_pattern.comment")
    public static String kmFormatPattern = "#,##0.000";

    @ConfigEntry(
            id = "kmFormatSeparator",
            type = EntryType.STRING,
            translation = "config.drifter.km_format_separator"
    )
    @Comment(value = "Symbols represent group and decimal separators.\nFirst - group, second - decimal.\nLength must be equal 2 symbols.",
            translation = "config.drifter.km_format_separator.comment")
    public static String kmFormatSeparator = " ,";

    public static final DecimalFormat kmFormat = new DecimalFormat();




    public static float startSpeed() {
        return distanceBetweenRingsKm / distanceBetweenRingsMinutes;
    }

    public static float minToTick(float min) {
        return min * 60 * 20;
    }

    public static float tickToMin(float ticks) {
        return ticks / 60 / 20;
    }

    @Contract(pure = true)
    public static @NotNull String timeToString(float timeInMinutes) {
        if (timeInMinutes == Float.MAX_VALUE || Float.isNaN(timeInMinutes) || Float.isInfinite(timeInMinutes))
            return "∞";
        float timeInSeconds = timeInMinutes % 1 * 60;
        float timeInHours = timeInSeconds / 60;
        if (timeInHours > 1) return String.format("%0,2d:%0,2d:%0,2d", (int) timeInHours, (int) timeInMinutes, (int) timeInSeconds);
        return String.format("%0,2d:%0,2d", (int) timeInMinutes, (int) (timeInSeconds));
    }

    @Contract(pure = true)
    public static @NotNull String kmToString(float distanceInKm) {
        return kmFormat.format(distanceInKm);
    }

    public static float brakeTick(float speed, float maxSpeed) {
        float braking = maxSpeed / minToTick(brakingTime);
        return Math.max(speed - braking, 0);
    }

    public static float accelerateTick(float speed, float maxSpeed) {
        float acceleration = maxSpeed / minToTick(accelerationTime);
        return Math.min(speed + acceleration, maxSpeed);
    }

}
