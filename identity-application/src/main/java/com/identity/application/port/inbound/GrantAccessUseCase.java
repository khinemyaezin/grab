package com.identity.application.port.inbound;

import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.model.write.AccessAssignmentResult;

public interface GrantAccessUseCase {
    AccessAssignmentResult execute(GrantAccessCommand command);
}
