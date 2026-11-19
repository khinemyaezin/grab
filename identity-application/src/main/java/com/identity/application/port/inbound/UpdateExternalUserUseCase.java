package com.identity.application.port.inbound;

import com.identity.application.model.write.ExternalUserProvisionResult;
import com.identity.application.model.write.UpdateExternalUserStatusCommand;

public interface UpdateExternalUserUseCase {
    ExternalUserProvisionResult execute(UpdateExternalUserStatusCommand command);
}
