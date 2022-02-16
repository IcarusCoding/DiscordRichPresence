package de.intelligence.drp.core.dc;

public final class DiscordConsts {

    public static final int MAX_RETRY_ATTEMPTS = 5;
    public static final int EXPONENTIAL_BACKOFF_INIT = 500;
    public static final int MAX_ACTIVITY_WAIT = 1000;
    public static final int FRAME_LENGTH = 32 * 1024;

    private DiscordConsts() {
        // prevent instantiation
    }

}
