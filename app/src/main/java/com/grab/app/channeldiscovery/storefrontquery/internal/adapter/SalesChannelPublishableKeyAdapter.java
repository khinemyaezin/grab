package com.grab.app.channeldiscovery.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.app.channeldiscovery.saleschannel.port.PublishableKeyQuery;
import com.grab.app.channeldiscovery.storefrontquery.internal.adapter.mapper.SalesChannelPublishableKeyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SalesChannelPublishableKeyAdapter implements SalesChannelPublishableKeyPort {
    private final PublishableKeyQuery publishableKeys;
    private final SalesChannelPublishableKeyMapper mapper;

    @Override
    public Optional<ResolvedChannel> resolve(String rawToken) {
        Optional<PublishableKeyQuery.Resolution> resolution = publishableKeys.resolve(rawToken);
        Optional<PublishableKeyQuery.Resolution> activeResolution = resolution
                .filter(PublishableKeyQuery.Resolution::keyActive);
        if (activeResolution.isEmpty()) {
            return Optional.empty();
        }
        PublishableKeyQuery.Resolution providerResolution = activeResolution.orElseThrow();
        ResolvedChannel resolvedChannel = mapper.toResolvedChannel(providerResolution);
        ResolvedChannel validatedChannel = validateChannel(resolvedChannel);
        return Optional.of(validatedChannel);
    }

    private ResolvedChannel validateChannel(ResolvedChannel channel) {
        if (channel == null || channel.salesChannelId() == null) {
            throw new IllegalStateException("Publishable key provider returned no channel identifier");
        }
        return channel;
    }
}
