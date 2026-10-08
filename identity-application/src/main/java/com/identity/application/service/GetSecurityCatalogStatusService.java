package com.identity.application.service;

import com.identity.application.model.read.GetSecurityCatalogStatusQuery;
import com.identity.application.model.read.SecurityCatalogStatusView;
import com.identity.application.port.inbound.GetSecurityCatalogStatusUseCase;
import com.identity.application.port.outbound.SecurityManifestQueryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetSecurityCatalogStatusService implements GetSecurityCatalogStatusUseCase {
    private final SecurityManifestQueryPort manifests;

    @Override
    public SecurityCatalogStatusView execute(GetSecurityCatalogStatusQuery query) {
        return manifests.status(query.moduleKey());
    }
}
