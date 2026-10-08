package com.grab.store.saleschannel.internal.api.adapter;

import com.grab.store.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.store.saleschannel.internal.api.adapter.mapper.SalesChannelSecurityManifestPublicationQueryMapper;
import com.grab.store.saleschannel.port.SalesChannelSecurityManifestPublicationQuery;
import com.saleschannel.application.port.inbound.GetSalesChannelSecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesChannelSecurityManifestPublicationQueryAdapter implements SalesChannelSecurityManifestPublicationQuery {
    private final GetSalesChannelSecurityManifestPublicationStatusUseCase useCase;
    private final SalesChannelSecurityManifestPublicationQueryMapper mapper;

    @Override
    @SalesChannelReadTransactional
    public PublicationStatus status() {
        var view = useCase.execute();
        return mapper.toResponse(view);
    }
}
