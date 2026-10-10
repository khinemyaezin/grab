package com.identity.application.port.inbound;

import com.identity.application.model.write.EnsureSecurityCatalogStateCommand;
import com.identity.application.model.write.EnsureSecurityCatalogStateResult;

public interface EnsureSecurityCatalogStateUseCase {
    EnsureSecurityCatalogStateResult execute(EnsureSecurityCatalogStateCommand command);
}
