package com.identity.application.port.inbound;

import com.identity.application.model.write.AcceptAccessInvitationCommand;
import com.identity.application.model.write.AccessAssignmentResult;

public interface AcceptAccessInvitationUseCase {
    AccessAssignmentResult execute(AcceptAccessInvitationCommand command);
}
