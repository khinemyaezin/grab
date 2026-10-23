package com.saleschannel.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.saleschannel.application.model.read.ResolvePublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.application.port.inbound.ResolvePublishableKeyUseCase;
import com.saleschannel.application.port.outbound.PublishableKeyQueryPort;
import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ResolvePublishableKeyService implements ResolvePublishableKeyUseCase {
    private static final Logger log = Loggers.getLogger(ResolvePublishableKeyService.class);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("pk_[A-Za-z0-9_-]{43}");

    private final PublishableKeyQueryPort keys;
    private final PublishableTokenGeneratorPort tokens;

    @Override
    public Optional<ResolvedPublishableKeyView> execute(ResolvePublishableKeyQuery query) {
        log.debug("Resolving publishable key");
        String rawToken = query.rawToken();
        if (rawToken == null || !TOKEN_PATTERN.matcher(rawToken).matches()) {
            return Optional.empty();
        }
        String digest = tokens.digest(rawToken);
        return keys.findActiveByDigest(digest).filter(ResolvedPublishableKeyView::keyActive);
    }
}
