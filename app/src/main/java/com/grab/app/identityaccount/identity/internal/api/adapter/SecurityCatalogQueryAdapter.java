package com.grab.app.identityaccount.identity.internal.api.adapter;

import com.grab.app.identityaccount.identity.internal.api.adapter.mapper.SecurityCatalogQueryMapper;
import com.grab.app.identityaccount.identity.internal.config.IdentityReadTransactional;
import com.grab.app.identityaccount.identity.port.SecurityCatalogQuery;
import com.identity.application.model.read.GetSecurityCatalogStatusQuery;
import com.identity.application.port.inbound.GetSecurityCatalogStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class SecurityCatalogQueryAdapter implements SecurityCatalogQuery {
    private final GetSecurityCatalogStatusUseCase useCase;
    private final SecurityCatalogQueryMapper mapper;

    @Override
    @IdentityReadTransactional(propagation = Propagation.REQUIRES_NEW)
    public SecurityCatalogStatus status(String moduleKey) {
        var query = new GetSecurityCatalogStatusQuery(moduleKey);
        var view = useCase.execute(query);
        return mapper.toResponse(view);
    }
}
