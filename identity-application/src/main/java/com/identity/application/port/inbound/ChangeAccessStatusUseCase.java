package com.identity.application.port.inbound;

import com.identity.application.model.write.ChangeAccessStatusCommand;
import com.identity.application.model.write.AccessAssignmentResult;

public interface ChangeAccessStatusUseCase {
    AccessAssignmentResult execute(ChangeAccessStatusCommand command);
}
