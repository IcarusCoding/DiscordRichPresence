package de.intelligence.drp.dc.pojo;

import com.google.gson.annotations.SerializedName;

public final class DiscordUser {

    private final long id;
    private final String username;
    private final int discriminator;
    private final String avatar;
    private final boolean bot;
    private final int flags;
    @SerializedName("premium_type")
    private final int premiumType;

    public DiscordUser(long id, String username, int discriminator, String avatar, boolean bot, int flags, int premiumType) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.bot = bot;
        this.flags = flags;
        this.premiumType = premiumType;
    }

    public long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public int getDiscriminator() {
        return this.discriminator;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public boolean isBot() {
        return this.bot;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getPremiumType() {
        return this.premiumType;
    }

    @Override
    public String toString() {
        return "DiscordUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", discriminator=" + discriminator +
                ", avatar='" + avatar + '\'' +
                ", bot=" + bot +
                ", flags=" + flags +
                ", premiumType=" + premiumType +
                '}';
    }

}
