package com.saleschannel.application.model.read;

import com.grab.framework.id.Id;

public record ResolvedPublishableKeyView(Id salesChannelId, boolean channelEnabled, boolean keyActive) {
}
