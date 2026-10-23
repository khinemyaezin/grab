package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;

public interface CreatePublishableKeyUseCase {
    CreatePublishableKeyResult execute(CreatePublishableKeyCommand command);
}
