package com.saleschannel.application.port.outbound;

import com.grab.framework.id.Id;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PublishableKeyQueryPort {
    Page<PublishableKeyView> list(Id salesChannelId, Pageable pageable);

    Optional<ResolvedPublishableKeyView> findActiveByDigest(String digest);
}
