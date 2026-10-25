package com.grab.app.productmerchandising.catalog.internal.api.adapter;

import com.grab.app.productmerchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.app.productmerchandising.catalog.internal.api.adapter.mapper.CatalogSecurityManifestPublicationQueryMapper;
import com.grab.app.productmerchandising.catalog.port.CatalogSecurityManifestPublicationQuery;
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
