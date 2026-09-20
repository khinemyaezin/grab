package com.cart.adapter.persistence.adapter;

import com.cart.adapter.persistence.entity.CartEntity;
import com.cart.adapter.persistence.entity.CartLineEntity;
import com.cart.adapter.persistence.repository.jpa.CartJpaRepository;
import com.cart.domain.enums.CartStatus;
import com.cart.domain.port.outbound.CartQueryPort;
import com.cart.domain.readmodel.CartLineView;
import com.cart.domain.readmodel.CartView;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CartQueryAdapter implements CartQueryPort {
    private final CartJpaRepository carts;
    private final PersistenceExecutor executor;

    @Override
    public Optional<CartView> findOpenByGuestToken(
            String guestToken,
            String salesChannelId,
            String regionId,
            CartStatus status
    ) {
        return executor.query("Cart", () -> carts.findByGuestTokenAndSalesChannelIdAndRegionIdAndStatus(
                guestToken,
                salesChannelId,
                regionId,
                status
        ).map(this::toView));
    }

    private CartView toView(CartEntity entity) {
        return new CartView(
                entity.getUuid(),
                entity.getGuestToken(),
                entity.getSalesChannelId(),
                entity.getChannelType(),
                entity.getRegionId(),
                entity.getCurrencyCode(),
                entity.getStatus(),
                entity.getLines().stream().map(this::toLineView).toList()
        );
    }

    private CartLineView toLineView(CartLineEntity line) {
        return new CartLineView(
                line.getUuid(),
                line.getVariantId(),
                line.getProductId(),
                line.getSellerId(),
                line.getTitle(),
                line.getSku(),
                line.getUnitPrice(),
                line.getQuantity()
        );
    }
}
