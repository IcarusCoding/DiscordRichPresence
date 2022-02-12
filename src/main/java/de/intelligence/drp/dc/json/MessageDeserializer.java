package de.intelligence.drp.dc.json;

import java.lang.reflect.Type;

import com.google.common.base.Enums;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.intelligence.drp.dc.proto.Command;
import de.intelligence.drp.dc.proto.Event;

public final class MessageDeserializer implements JsonDeserializer<Message> {

    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject obj = json.getAsJsonObject();
        final Command cmd = obj.has("cmd") ? Enums.getIfPresent(Command.class, obj.get("cmd").getAsString()).toJavaUtil().orElse(Command.INVALID) : null;
        final Event evt = obj.has("evt") && !obj.get("evt").isJsonNull() ? Enums.getIfPresent(Event.class, obj.get("evt").getAsString()).toJavaUtil().orElse(Event.INVALID) : null;
        return new Message(cmd, obj.has("data") ? obj.get("data").getAsJsonObject() : null, evt, obj.has("nonce") ? (!obj.get("nonce").isJsonNull() ? obj.get("nonce").getAsString() : null) : null);
    }

}
