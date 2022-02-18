package de.intelligence.drp.core.dc;

public final class DiscordConsts {

    public static final int MAX_RETRY_ATTEMPTS = 5;
    public static final int EXPONENTIAL_BACKOFF_INIT = 500;
    public static final int MAX_ACTIVITY_WAIT = 500;
    public static final int FRAME_LENGTH = 32 * 1024;
    public static final int OUTBOUND_DELAY = 5000;
    public static final int HEADER_SIZE = 8;

    private DiscordConsts() {
        // prevent instantiation
    }

}
