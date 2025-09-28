package net.smok.drifter.utils;

public final class FlyUtils {
    public static final int ACCELERATION = 1; // km/t2
    public static final int BRAKING = 1; // km/t2

    public static float fuelCost(int maxSpeed, int totalDistance, int fuelEfficiency) {
        return totalTime(maxSpeed, totalDistance) / fuelEfficiency;
    }

    public static String timeToString(float timeInTicks) {

        float timeInSeconds = timeInTicks / 20;
        float timeInMinutes = timeInSeconds / 60;
        return String.format("%0,2d:%0,2d", (int) timeInMinutes, (int) (timeInSeconds % 60));
    }

    public static float leftTime(int maxSpeed, int totalDistance, int speed) {
        if (totalDistance == 0) return 0;
        float brakingDistance = brakingDistance(maxSpeed);
        float accelerationDistance = accelerationDistance(maxSpeed);
        float constSpeedDistance = constSpeedDistance(maxSpeed, totalDistance);
        if (totalDistance < brakingDistance) return (float) speed / BRAKING;
        if (totalDistance < brakingDistance + constSpeedDistance)
            return (totalDistance - brakingDistance) / maxSpeed + (float) maxSpeed / BRAKING;
        return (float) speed / ACCELERATION + (totalDistance - brakingDistance - accelerationDistance) / maxSpeed + (float) maxSpeed / BRAKING;
    }

    public static float totalTime(int maxSpeed, int totalDistance) {
        float t1 = constSpeedTime(maxSpeed, totalDistance);
        float t2 = accelerationTime(maxSpeed);
        float t3 = brakingTime(maxSpeed);
        return t1 + t2 + t3;
    }

    public static float constSpeedDistance(int maxSpeed, int totalDistance) {
        float v1 = accelerationDistance(maxSpeed);
        float v2 = brakingDistance(maxSpeed);
        return totalDistance - v1 - v2;
    }

    public static float constSpeedTime(int maxSpeed, int totalDistance) {
        float v1 = accelerationDistance(maxSpeed);
        float v2 = brakingDistance(maxSpeed);
        return (totalDistance - v1 - v2) / maxSpeed;
    }

    public static float accelerationDistance(int maxSpeed) {
        return (float) maxSpeed * maxSpeed / ACCELERATION / 2;
    }

    public static float accelerationTime(int maxSpeed) {
        return (float) maxSpeed / ACCELERATION;
    }

    public static float brakingDistance(int maxSpeed) {
        return (float) maxSpeed * maxSpeed / BRAKING / 2;
    }

    public static float brakingTime(int maxSpeed) {
        return (float) maxSpeed / BRAKING;
    }

    public static int speedToKm(int i) {
        return i * 20 * 60;
    }
}
