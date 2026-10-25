package com.grab.app.channeldiscovery.saleschannel.internal.api.adapter;

import com.grab.app.channeldiscovery.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.app.channeldiscovery.saleschannel.internal.api.adapter.mapper.SalesChannelSecurityManifestPublicationQueryMapper;
import com.grab.app.channeldiscovery.saleschannel.port.SalesChannelSecurityManifestPublicationQuery;
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
