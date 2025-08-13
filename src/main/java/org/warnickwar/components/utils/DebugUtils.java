package org.warnickwar.components.utils;

import org.warnickwar.components.Constants;
import org.warnickwar.components.platform.Services;

// TBH, IDK why I'm writing this class. I'm gonna continue anyway.
public final class DebugUtils {

    public static void logIfDebug(String value) {
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            Constants.LOGGER.debug(value);
        }
    }

    public static void errorQuietly(String value) {
        Constants.LOGGER.error(value);
    }

    public static void errorQuietly(String value, Throwable throwable) {
        Constants.LOGGER.error(value, throwable);
    }

    // This is such a wack hack to make it not have a compile error lmao
    public static <T extends Throwable> void errorInEditor(T throwable) throws T {
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            throw throwable;
        } else {
            errorQuietly("Hiding Error: ", throwable);
        }
    }

}
