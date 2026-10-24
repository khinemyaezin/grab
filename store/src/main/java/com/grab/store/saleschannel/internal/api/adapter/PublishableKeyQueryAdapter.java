package com.grab.store.saleschannel.internal.api.adapter;

import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.internal.api.adapter.mapper.PublishableKeyQueryMapper;
import com.grab.store.saleschannel.port.PublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvePublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.application.port.inbound.ResolvePublishableKeyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PublishableKeyQueryAdapter implements PublishableKeyQuery {
    private final ResolvePublishableKeyUseCase useCase;
    private final PublishableKeyQueryMapper mapper;

    @Override
    @SalesChannelReadTransactional
    public Optional<Resolution> resolve(String rawToken) {
        Optional<ResolvedPublishableKeyView> result = useCase.execute(new ResolvePublishableKeyQuery(rawToken));
        return result.map(mapper::toResolution);
    }
}
