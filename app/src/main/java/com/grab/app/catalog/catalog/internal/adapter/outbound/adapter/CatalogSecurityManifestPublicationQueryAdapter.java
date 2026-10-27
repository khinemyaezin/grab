package com.grab.app.catalog.catalog.internal.adapter.outbound;

import com.grab.app.catalog.catalog.internal.config.CatalogReadTransactional;
import com.grab.app.catalog.catalog.internal.adapter.outbound.mapper.CatalogSecurityManifestPublicationQueryMapper;
import com.grab.app.catalog.catalog.port.CatalogSecurityManifestPublicationQuery;
import com.catalog.application.port.inbound.GetCatalogSecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogSecurityManifestPublicationQueryAdapter implements CatalogSecurityManifestPublicationQuery {
    private final GetCatalogSecurityManifestPublicationStatusUseCase useCase;
    private final CatalogSecurityManifestPublicationQueryMapper mapper;

    @Override
    @CatalogReadTransactional
    public PublicationStatus status() {
        var view = useCase.execute();
        return mapper.toResponse(view);
    }
}
