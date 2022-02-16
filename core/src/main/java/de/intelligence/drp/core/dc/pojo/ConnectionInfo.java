package de.intelligence.drp.core.dc.pojo;

import com.google.gson.annotations.SerializedName;

public record ConnectionInfo(@SerializedName("v") int rpcVersion, @SerializedName("client_id") String applicationId) {}