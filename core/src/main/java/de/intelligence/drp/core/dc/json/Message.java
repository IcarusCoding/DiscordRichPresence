package de.intelligence.drp.core.dc.json;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import de.intelligence.drp.core.dc.proto.Command;
import de.intelligence.drp.core.dc.proto.EventTypes;

public final class Message {

    @SerializedName("cmd")
    private final Command command;

    private final JsonObject data;

    @SerializedName("evt")
    private final EventTypes event;

    private final String nonce;

    public Message(Command command, JsonObject data, EventTypes event, String nonce) {
        this.command = command == null ? Command.NONE : command;
        this.data = data;
        this.event = event == null ? EventTypes.NONE : event;
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "Message{" +
                "command=" + command +
                ", event=" + event +
                ", data=" + data +
                ", nonce='" + nonce + '\'' +
                '}';
    }

    public Command getCommand() {
        return this.command;
    }

    public JsonObject getData() {
        return this.data;
    }

    public EventTypes getEvent() {
        return this.event;
    }

    public String getNonce() {
        return this.nonce;
    }

    public boolean hasNonce() {
        return this.nonce != null;
    }

}
