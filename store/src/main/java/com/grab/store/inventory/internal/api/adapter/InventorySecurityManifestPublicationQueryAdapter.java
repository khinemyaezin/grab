package com.grab.store.inventory.internal.api.adapter;

import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.api.adapter.mapper.InventorySecurityManifestPublicationQueryMapper;
import com.grab.store.inventory.port.InventorySecurityManifestPublicationQuery;
import com.inventory.application.port.inbound.GetInventorySecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventorySecurityManifestPublicationQueryAdapter implements InventorySecurityManifestPublicationQuery {
    private final GetInventorySecurityManifestPublicationStatusUseCase useCase;
    private final InventorySecurityManifestPublicationQueryMapper mapper;

    @Override
    @InventoryReadTransactional
    public PublicationStatus status() {
        var view = useCase.execute();
        return mapper.toResponse(view);
    }
}
