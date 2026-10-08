package com.identity.application.port.inbound;

import com.identity.application.model.write.RevalidateWaitingSecurityManifestsCommand;

public interface RevalidateWaitingSecurityManifestsUseCase {
    void execute(RevalidateWaitingSecurityManifestsCommand command);
}
