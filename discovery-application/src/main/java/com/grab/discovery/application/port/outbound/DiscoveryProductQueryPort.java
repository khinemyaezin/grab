package com.grab.discovery.application.port.outbound;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.framework.id.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DiscoveryProductQueryPort {
    Page<DiscoveryProductView> searchVisible(Id salesChannelId, String keyword, Id categoryId, Pageable pageable);

    Optional<DiscoveryProductView> findVisibleBySlug(Id salesChannelId, String slug);
}
