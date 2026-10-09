package com.identity.application.port.inbound;

import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.FulfillAdminAccessAssignmentCommand;

public interface FulfillAdminAccessAssignmentUseCase {
    AccessAssignmentResult execute(FulfillAdminAccessAssignmentCommand command);
}
