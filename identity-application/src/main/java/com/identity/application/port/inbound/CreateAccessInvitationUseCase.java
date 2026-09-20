package com.identity.application.port.inbound;

import com.identity.application.model.write.CreateAccessInvitationCommand;
import com.identity.application.model.write.AccessInvitationResult;

public interface CreateAccessInvitationUseCase {
    AccessInvitationResult execute(CreateAccessInvitationCommand command);
}
