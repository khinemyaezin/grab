package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.read.ResolvePublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;

import java.util.Optional;

public interface ResolvePublishableKeyUseCase {
    Optional<ResolvedPublishableKeyView> execute(ResolvePublishableKeyQuery query);
}
