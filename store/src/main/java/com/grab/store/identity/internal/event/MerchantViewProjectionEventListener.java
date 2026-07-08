package com.grab.store.identity.internal.event;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.merchant.events.MerchantApprovedIntegrationEvent;
import com.identity.infrastructure.entity.MerchantViewEntity;
import com.identity.infrastructure.repository.jpa.MerchantViewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MerchantViewProjectionEventListener {
    private static final Logger log = Loggers.getLogger(MerchantViewProjectionEventListener.class);
    private final MerchantViewJpaRepository merchantViewRepository;

    @EventListener
    @IdentityTransactional
    public void onMerchantApproved(MerchantApprovedIntegrationEvent event) {
        log.info("Handling merchant approved event: {}", event);

        var view = new MerchantViewEntity();
        view.setScopeId(event.merchantId());
        view.setName(event.merchantName());
        view.setStatus(event.status());
        view.setCreatedAt(LocalDateTime.now());
        view.setUpdatedAt(LocalDateTime.now());
        merchantViewRepository.save(view);
    }
}
