package de.intelligence.drp.api;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public final class RichPresence implements JsonSerializable, Updatable {

    private final boolean autoUpdate;
    private final Set<Observer> observers;

    private String state;
    private String details;
    private long startTime;
    private long endTime;
    private String partyId;
    private int partySize;
    private int partySizeMax;
    private String joinSecret;
    private String matchSecret;
    private String spectateSecret;
    private String smallImage;
    private String largeImage;

    private RichPresence(String state, String details, long startTime, long endTime, String partyId, int partySize,
                         int partySizeMax, String joinSecret, String matchSecret, String spectateSecret,
                         String smallImage, String largeImage, boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        this.observers = new HashSet<>();
        this.state = state;
        this.details = details;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partyId = partyId;
        this.partySize = partySize;
        this.partySizeMax = partySizeMax;
        this.joinSecret = joinSecret;
        this.matchSecret = matchSecret;
        this.spectateSecret = spectateSecret;
        this.smallImage = smallImage;
        this.largeImage = largeImage;
    }

    @Override
    public JSONObject convertToJson() {
        final JSONObject object = new JSONObject();
        if (this.state != null) {
            object.put("state", this.state);
        }
        if (this.details != null) {
            object.put("details", this.details);
        }
        if (this.startTime > 0) {
            final JSONObject timestamps = new JSONObject()
                    .put("start", this.startTime);
            if(this.endTime > 0 && this.startTime < this.endTime) {
                timestamps.put("end", this.endTime);
            }
            object.put("timestamps", timestamps);
        }
        if (this.partyId != null && this.partySize > -1 && this.partySizeMax >= this.partySize) {
            object.put("party", new JSONObject()
                    .put("id", this.partyId)
                    .put("size", new JSONArray()
                            .put(this.partySize)
                            .put(this.partySizeMax)));
        }
        final JSONObject secretObj = new JSONObject();
        if (this.joinSecret != null) {
            secretObj.put("join", this.joinSecret);
        }
        if (this.matchSecret != null) {
            secretObj.put("match", this.matchSecret);
        }
        if (this.spectateSecret != null) {
            secretObj.put("spectate", this.spectateSecret);
        }
        if(!secretObj.keySet().isEmpty()) {
            object.put("secrets", secretObj);
        }
        final JSONObject assetObj = new JSONObject();
        if(this.smallImage != null) {
            assetObj.put("small_image", this.smallImage);
        }
        if(this.largeImage != null) {
            assetObj.put("large_image", this.largeImage);
        }
        if(!assetObj.keySet().isEmpty()) {
            object.put("assets", assetObj);
        }
        return object;
    }

    @Override
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void update() {
        if (this.autoUpdate) {
            this.observers.forEach(o -> o.notifyUpdate(this));
        }
    }

    public String getState() {
        return this.state;
    }

    public String getDetails() {
        return this.details;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public String getPartyId() {
        return this.partyId;
    }

    public int getPartySize() {
        return this.partySize;
    }

    public int getPartySizeMax() {
        return this.partySizeMax;
    }

    public String getJoinSecret() {
        return this.joinSecret;
    }

    public String getMatchSecret() {
        return this.matchSecret;
    }

    public String getSpectateSecret() {
        return this.spectateSecret;
    }

    public String getSmallImage() {
        return this.smallImage;
    }

    public String getLargeImage() {
        return this.largeImage;
    }

    public void setState(String state) {
        this.state = state;
        this.update();
    }

    public void setDetails(String details) {
        this.details = details;
        this.update();
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        this.update();
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.update();
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
        this.update();
    }

    public void setPartySize(int partySize) {
        this.partySize = partySize;
        this.update();
    }

    public void setPartySizeMax(int partySizeMax) {
        this.partySizeMax = partySizeMax;
        this.update();
    }

    public void setJoinSecret(String joinSecret) {
        this.joinSecret = joinSecret;
        this.update();
    }

    @Deprecated
    public void setMatchSecret(String matchSecret) {
        this.matchSecret = matchSecret;
        this.update();
    }

    @Deprecated
    public void setSpectateSecret(String spectateSecret) {
        this.spectateSecret = spectateSecret;
        this.update();
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
        this.update();
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
        this.update();
    }

    public void clear() {
        this.state = null;
        this.details = null;
        this.startTime = 0;
        this.endTime = 0;
        this.partyId = null;
        this.partySize = 0;
        this.partySizeMax = 0;
        this.joinSecret = null;
        this.matchSecret = null;
        this.spectateSecret = null;
        this.smallImage = null;
        this.largeImage = null;
        this.update();
    }

    public static final class Builder {

        private String state;
        private String details;
        private long startTime;
        private long endTime;
        private String partyId;
        private int partySize;
        private int partySizeMax;
        private String joinSecret;
        private String matchSecret;
        private String spectateSecret;
        private String smallImage;
        private String largeImage;
        private boolean autoUpdate;

        public RichPresence build() {
            return new RichPresence(this.state, this.details, this.startTime, this.endTime, this.partyId, this.partySize,
                    this.partySizeMax, this.joinSecret, this.matchSecret, this.spectateSecret, this.smallImage,
                    this.largeImage, this.autoUpdate);
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder setStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder setPartyId(String partyId) {
            this.partyId = partyId;
            return this;
        }

        public Builder setPartySize(int partySize) {
            this.partySize = partySize;
            return this;
        }

        public Builder setPartySizeMax(int partySizeMax) {
            this.partySizeMax = partySizeMax;
            return this;
        }

        public Builder setJoinSecret(String joinSecret) {
            this.joinSecret = joinSecret;
            return this;
        }

        @Deprecated
        public Builder setMatchSecret(String matchSecret) {
            this.matchSecret = matchSecret;
            return this;
        }

        @Deprecated
        public Builder setSpectateSecret(String spectateSecret) {
            this.spectateSecret = spectateSecret;
            return this;
        }

        public Builder setSmallImage(String smallImage) {
            this.smallImage = smallImage;
            return this;
        }

        public Builder setLargeImage(String largeImage) {
            this.largeImage = largeImage;
            return this;
        }

        public Builder withAutoUpdate() {
            this.autoUpdate = true;
            return this;
        }

    }

}
