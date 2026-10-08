package com.identity.application.port.inbound;

import com.identity.application.model.read.GetSecurityCatalogStatusQuery;
import com.identity.application.model.read.SecurityCatalogStatusView;

public interface GetSecurityCatalogStatusUseCase {
    SecurityCatalogStatusView execute(GetSecurityCatalogStatusQuery query);
}
