package de.intelligence.drp.api.user;

public interface IDiscordUser {

    long getId();

    String getUsername();

    int getDiscriminator();

    String getFullUsername();

    String getAvatar();

    boolean isBot();

    int getFlags();

    int getPremiumType();

}
