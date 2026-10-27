package com.grab.app.channel.saleschannel.internal.adapter.outbound;

import com.grab.app.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.grab.app.channel.saleschannel.internal.adapter.outbound.mapper.SalesChannelSecurityManifestPublicationQueryMapper;
import com.grab.app.channel.saleschannel.port.SalesChannelSecurityManifestPublicationQuery;
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
