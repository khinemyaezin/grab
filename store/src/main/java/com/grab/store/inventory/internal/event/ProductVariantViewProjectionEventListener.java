package com.grab.store.inventory.internal.event;

import com.grab.store.catalog.events.ProductDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductNameChangedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantAddedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantRestoredIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantUpdatedIntegrationEvent;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductVariantViewProjectionEventListener {

    private final ProductVariantViewJpaRepository productVariantViewRepository;

    @EventListener
    @InventoryTransactional
    public void onVariantAdded(ProductVariantAddedIntegrationEvent event) {
        log.info("Projecting variant added: productId={}, variantId={}", event.productId(), event.variantId());

        ProductVariantViewEntity view = productVariantViewRepository.findByVariantUuid(event.variantId())
                .orElseGet(ProductVariantViewEntity::new);
        view.setVariantUuid(event.variantId());
        view.setProductUuid(event.productId());
        view.setSku(event.sku());
        view.setProductName(event.productName());
        view.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
        productVariantViewRepository.save(view);
    }

    @EventListener
    @InventoryTransactional
    public void onVariantUpdated(ProductVariantUpdatedIntegrationEvent event) {
        log.info("Projecting variant updated: productId={}, variantId={}", event.productId(), event.variantId());

        ProductVariantViewEntity view = productVariantViewRepository.findByVariantUuid(event.variantId())
                .orElseGet(() -> {
                    var created = new ProductVariantViewEntity();
                    created.setVariantUuid(event.variantId());
                    created.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
                    return created;
                });
        view.setProductUuid(event.productId());
        view.setSku(event.sku());
        productVariantViewRepository.save(view);
    }

    @EventListener
    @InventoryTransactional
    public void onVariantDeleted(ProductVariantDeletedIntegrationEvent event) {
        log.info("Projecting variant deleted: productId={}, variantId={}", event.productId(), event.variantId());

        productVariantViewRepository.findByVariantUuid(event.variantId()).ifPresent(view -> {
            view.setStatus(ProductVariantViewEntity.STATUS_DELETED);
            productVariantViewRepository.save(view);
        });
    }

    @EventListener
    @InventoryTransactional
    public void onVariantRestored(ProductVariantRestoredIntegrationEvent event) {
        log.info("Projecting variant restored: productId={}, variantId={}", event.productId(), event.variantId());

        productVariantViewRepository.findByVariantUuid(event.variantId()).ifPresent(view -> {
            view.setStatus(ProductVariantViewEntity.STATUS_ACTIVE);
            productVariantViewRepository.save(view);
        });
    }

    @EventListener
    @InventoryTransactional
    public void onProductNameChanged(ProductNameChangedIntegrationEvent event) {
        log.info("Projecting product name change: productId={}", event.productId());

        var views = productVariantViewRepository.findAllByProductUuid(event.productId());
        views.forEach(view -> view.setProductName(event.newName()));
        productVariantViewRepository.saveAll(views);
    }

    @EventListener
    @InventoryTransactional
    public void onProductDeleted(ProductDeletedIntegrationEvent event) {
        log.info("Projecting product deleted: productId={}", event.productId());

        var views = productVariantViewRepository.findAllByProductUuid(event.productId());
        views.forEach(view -> view.setStatus(ProductVariantViewEntity.STATUS_DELETED));
        productVariantViewRepository.saveAll(views);
    }
}
