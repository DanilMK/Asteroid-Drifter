package net.smok.drifter;

import net.smok.drifter.registries.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Debug {


    private static boolean[] cached;

    public static void startOrderedLog(int size) {
        cached = new boolean[size];
    }

    public static void orderedLog(int index, String log) {
        if (cached != null && !cached[index]) {
            Debug.log(log);
            cached[index] = true;
        }
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(Values.MOD_ID);

    public static void log(String message) {
        LOGGER.info(message);
    }

    public static void log(String message, Object... objects) {
        LOGGER.info(message, objects);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void warn(String message, Object... objects) {
        LOGGER.warn(message, objects);
    }

    public static void warn(String message, Throwable throwable) {
        LOGGER.warn(message, throwable);
    }

    public static void err(String message) {
        LOGGER.error(message);
    }

    public static void err(String message, Object... objects) {
        LOGGER.error(message, objects);
    }

    public static void err(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }
}
