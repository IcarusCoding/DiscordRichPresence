package de.intelligence.drp.dc.pojo;

import com.google.gson.annotations.SerializedName;

public record ConnectionInfo(@SerializedName("v") int rpcVersion, @SerializedName("client_id") String applicationId) {}
