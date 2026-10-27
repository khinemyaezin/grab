package com.grab.app.account.identity.internal.adapter.outbound;

import com.grab.app.account.identity.internal.adapter.outbound.mapper.SecurityCatalogQueryMapper;
import com.grab.app.account.identity.internal.config.IdentityReadTransactional;
import com.grab.app.account.identity.port.SecurityCatalogQuery;
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
