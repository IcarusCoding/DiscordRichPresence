package de.intelligence.drp.core.dc.pojo;

import com.google.gson.annotations.SerializedName;

import de.intelligence.drp.api.user.IDiscordUser;

public final class DiscordUserImpl implements IDiscordUser {

    private final long id;
    private final String username;
    private final int discriminator;
    private final String avatar;
    private final boolean bot;
    private final int flags;
    @SerializedName("premium_type")
    private final int premiumType;

    // initialized by gson
    private DiscordUserImpl(long id, String username, int discriminator, String avatar, boolean bot, int flags, int premiumType) {
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.bot = bot;
        this.flags = flags;
        this.premiumType = premiumType;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public int getDiscriminator() {
        return this.discriminator;
    }

    @Override
    public String getFullUsername() {
        return this.username + "#" + this.discriminator;
    }

    @Override
    public String getAvatar() {
        return this.avatar;
    }

    @Override
    public boolean isBot() {
        return this.bot;
    }

    @Override
    public int getFlags() {
        return this.flags;
    }

    @Override
    public int getPremiumType() {
        return this.premiumType;
    }

    //TODO remove
    @Override
    public String toString() {
        return "DiscordUserImpl{" +
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
