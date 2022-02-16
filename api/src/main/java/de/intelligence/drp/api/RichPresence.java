package de.intelligence.drp.api;

import org.json.JSONArray;
import org.json.JSONObject;

public final class RichPresence implements JsonSerializable {

    private final String state;
    private final String details;
    private final long startTime;
    private final long endTime;
    private final String partyId;
    private final int partySize;
    private final int partySizeMax;

    private RichPresence(String state, String details, long startTime, long endTime, String partyId, int partySize, int partySizeMax) {
        this.state = state;
        this.details = details;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partyId = partyId;
        this.partySize = partySize;
        this.partySizeMax = partySizeMax;
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

    @Override
    public JSONObject convertToJson() {
        return new JSONObject()
                .put("state", this.state)
                .put("details", this.details)
                .put("timestamps", new JSONObject()
                        .put("start", this.startTime)
                        .put("end", this.endTime))
                .put("party", new JSONObject()
                        .put("id", this.partyId)
                        .put("size", new JSONArray()
                                .put(this.partySize)
                                .put(this.partySizeMax)));
    }

    public static final class Builder {

        private String state;
        private String details;
        private long startTime;
        private long endTime;
        private String partyId;
        private int partySize;
        private int partySizeMax;

        public RichPresence build() {
            return new RichPresence(this.state, this.details, this.startTime, this.endTime, this.partyId, this.partySize, this.partySizeMax);
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

    }

}
