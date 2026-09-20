package com.identity.application.port.inbound;

import com.identity.application.model.write.CancelAccessInvitationCommand;
import com.identity.application.model.write.AccessInvitationResult;

public interface CancelAccessInvitationUseCase {
    AccessInvitationResult execute(CancelAccessInvitationCommand command);
}
