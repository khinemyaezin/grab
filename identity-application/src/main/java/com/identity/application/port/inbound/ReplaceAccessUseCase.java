package com.identity.application.port.inbound;

import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.model.write.AccessAssignmentResult;

public interface ReplaceAccessUseCase {
    AccessAssignmentResult execute(ReplaceAccessCommand command);
}
