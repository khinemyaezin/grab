package com.grab.app.identityaccount.identity.internal.api.adapter;

import com.grab.app.identityaccount.identity.internal.config.IdentityReadTransactional;
import com.grab.app.identityaccount.identity.internal.api.adapter.mapper.IdentitySecurityManifestPublicationQueryMapper;
import com.grab.app.identityaccount.identity.port.IdentitySecurityManifestPublicationQuery;
import com.identity.application.port.inbound.GetIdentitySecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentitySecurityManifestPublicationQueryAdapter implements IdentitySecurityManifestPublicationQuery {
    private final GetIdentitySecurityManifestPublicationStatusUseCase useCase;
    private final IdentitySecurityManifestPublicationQueryMapper mapper;

    @Override
    @IdentityReadTransactional
    public PublicationStatus status() {
        var view = useCase.execute();
        return mapper.toResponse(view);
    }
}
