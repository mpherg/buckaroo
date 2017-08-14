package com.loopperfect.buckaroo;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public final class BitBucketServerConfig {
    public String sshCloneUrl;
    public String browseUrl;

    public BitBucketServerConfig(final String _sshCloneUrl, final String _browseUrl) {
        this.sshCloneUrl = Preconditions.checkNotNull(_sshCloneUrl);
        this.browseUrl = Preconditions.checkNotNull(_browseUrl);
    }

}
