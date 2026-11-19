package com.identity.application.port.inbound;

import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.ProvisionExternalUserCommand;

public interface ProvisionExternalUserUseCase {
    ExternalUserProvisionResult execute(ProvisionExternalUserCommand command);
}
