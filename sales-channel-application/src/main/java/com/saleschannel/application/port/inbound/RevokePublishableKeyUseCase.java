package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.write.RevokePublishableKeyCommand;

public interface RevokePublishableKeyUseCase {
    Void execute(RevokePublishableKeyCommand command);
}
