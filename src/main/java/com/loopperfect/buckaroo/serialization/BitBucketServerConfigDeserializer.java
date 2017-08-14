package com.loopperfect.buckaroo.serialization;

import com.google.gson.*;
import com.loopperfect.buckaroo.BitBucketServerConfig;
import java.lang.reflect.Type;

public final class BitBucketServerConfigDeserializer implements JsonDeserializer<BitBucketServerConfig> {

    @Override
    public BitBucketServerConfig deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new BitBucketServerConfig(
                obj.get("sshcloneurl").getAsString(),
                obj.get("browseurl").getAsString()
        );
    }
}
