package de.intelligence.drp.core.os;

public final class OSUtils {

    private static final OS CURRENT_OS;
    private static final OSDependent OS_DEPENDENT;

    static {
        final String fullOS = System.getProperty("os.name").toLowerCase();
        if (fullOS.contains("win")) {
            CURRENT_OS = OS.WIN;
            OS_DEPENDENT = new WindowsDependent();
        } else if (fullOS.contains("mac")) {
            CURRENT_OS = OS.MAC;
            OS_DEPENDENT = new MacDependent();
        } else if (fullOS.contains("nux")) {
            CURRENT_OS = OS.NUX;
            OS_DEPENDENT = new LinuxDependent();
        } else {
            CURRENT_OS = OS.UNKNOWN;
            OS_DEPENDENT = null;
        }
    }

    // prevent instantiation
    private OSUtils() {}

    public static boolean isWin() {
        return CURRENT_OS == OS.WIN;
    }

    public static boolean isMac() {
        return CURRENT_OS == OS.MAC;
    }

    public static boolean isLinux() {
        return CURRENT_OS == OS.NUX;
    }

    public static OS getCurrentOS() {
        return CURRENT_OS;
    }

    public static OSDependent getOsDependent() {
        return OS_DEPENDENT;
    }

    public enum OS {
        WIN,
        MAC,
        NUX,
        UNKNOWN
    }

}
