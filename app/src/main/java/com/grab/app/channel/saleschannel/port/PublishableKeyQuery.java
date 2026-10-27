package com.grab.app.channel.saleschannel.port;

import java.util.Optional;

public interface PublishableKeyQuery {
    Optional<Resolution> resolve(String rawToken);

    record Resolution(String salesChannelId, boolean channelEnabled, boolean keyActive) {
    }
}
