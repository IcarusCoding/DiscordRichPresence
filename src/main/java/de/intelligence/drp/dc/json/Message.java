package de.intelligence.drp.dc.json;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import de.intelligence.drp.dc.proto.Command;
import de.intelligence.drp.dc.proto.Event;

public class Message {

    @SerializedName("cmd")
    private final Command command;

    private final JsonObject data;

    @SerializedName("evt")
    private final Event event;

    private final String nonce;

    public Message(Command command, JsonObject data, Event event, String nonce) {
        this.command = command == null ? Command.NONE : command;
        this.data = data;
        this.event = event == null ? Event.NONE : event;
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "Response{" +
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

    public Event getEvent() {
        return this.event;
    }

    public String getNonce() {
        return this.nonce;
    }

    public boolean hasNonce() {
        return this.nonce != null;
    }

}
