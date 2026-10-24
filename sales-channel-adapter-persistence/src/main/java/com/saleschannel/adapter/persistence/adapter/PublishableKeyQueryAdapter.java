package com.saleschannel.adapter.persistence.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import com.saleschannel.adapter.persistence.specification.jpa.PublishableKeyQuerySpecification;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.application.port.outbound.PublishableKeyQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class PublishableKeyQueryAdapter implements PublishableKeyQueryPort {
    private static final Logger log = Loggers.getLogger(PublishableKeyQueryAdapter.class);
    private final PublishableKeyQuerySpecification specification;
    private final PersistenceExecutor executor;

    @Override
    public Page<PublishableKeyView> list(Id salesChannelId, Pageable pageable) {
        String channelId = salesChannelId.getValue();
        log.debug("Querying publishable key metadata for salesChannelId={}", channelId);
        return executor.query("PublishableKey", () -> specification.list(channelId, pageable));
    }

    @Override
    public Optional<ResolvedPublishableKeyView> findActiveByDigest(String digest) {
        log.debug("Querying active publishable key channel metadata");
        return executor.query("PublishableKey", () ->
                specification.findActiveByDigest(digest));
    }
}
