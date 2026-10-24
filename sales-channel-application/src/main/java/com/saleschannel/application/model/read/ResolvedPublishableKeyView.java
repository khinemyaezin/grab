package com.saleschannel.application.model.read;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;

public record ResolvedPublishableKeyView(Id salesChannelId, boolean channelEnabled, boolean keyActive) {
    public ResolvedPublishableKeyView(String salesChannelId, boolean channelEnabled, boolean keyActive) {
        this(new CommonId(salesChannelId), channelEnabled, keyActive);
    }
}
