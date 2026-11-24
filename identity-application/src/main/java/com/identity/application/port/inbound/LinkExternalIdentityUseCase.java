package com.identity.application.port.inbound;

import com.identity.application.model.write.ExternalIdentityLinkResult;
import com.identity.application.model.write.LinkExternalIdentityCommand;

public interface LinkExternalIdentityUseCase {
    ExternalIdentityLinkResult execute(LinkExternalIdentityCommand command);
}
