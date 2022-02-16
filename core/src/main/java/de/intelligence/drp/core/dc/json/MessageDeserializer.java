package de.intelligence.drp.core.dc.json;

import java.lang.reflect.Type;

import com.google.common.base.Enums;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.intelligence.drp.core.dc.proto.Command;
import de.intelligence.drp.core.dc.proto.EventTypes;

public final class MessageDeserializer implements JsonDeserializer<Message> {

    @Override
    public Message deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject obj = jsonElement.getAsJsonObject();
        final Command cmd = obj.has("cmd") ? Enums.getIfPresent(Command.class, obj.get("cmd").getAsString()).toJavaUtil().orElse(Command.INVALID) : null;
        final EventTypes evt = obj.has("evt") && !obj.get("evt").isJsonNull() ? Enums.getIfPresent(EventTypes.class, obj.get("evt").getAsString()).toJavaUtil().orElse(EventTypes.INVALID) : null;
        return new Message(cmd, obj.has("data") ? obj.get("data").getAsJsonObject() : null, evt, obj.has("nonce") ? (!obj.get("nonce").isJsonNull() ? obj.get("nonce").getAsString() : null) : null);
    }

}
