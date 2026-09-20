package com.cart.infrastructure.repository.jpa.impl;

import com.cart.domain.aggregate.Cart;
import com.cart.domain.enums.CartStatus;
import com.cart.domain.repository.CartRepository;
import com.cart.infrastructure.entity.CartEntity;
import com.cart.infrastructure.mapper.jpa.CartJpaAssembler;
import com.cart.infrastructure.repository.jpa.CartJpaRepository;
import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultCartRepository implements CartRepository {
    private final CartJpaRepository carts;
    private final CartJpaAssembler assembler;
    private final DomainEventProducer events;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Cart> findById(Id id) {
        return executor.query("Cart", () -> carts.findByUuid(id.getValue()).map(assembler::toDomain));
    }

    @Override
    public Optional<Cart> findOpenByGuestToken(String guestToken, Id salesChannelId, Id regionId, CartStatus status) {
        return executor.query("Cart", () -> carts.findByGuestTokenAndSalesChannelIdAndRegionIdAndStatus(
                guestToken,
                salesChannelId.getValue(),
                regionId == null ? null : regionId.getValue(),
                status
        ).map(assembler::toDomain));
    }

    @Override
    public Cart save(Cart cart) {
        return executor.command("Cart", () -> {
            CartEntity existing = carts.findByUuid(cart.getId().getValue()).orElse(null);
            CartEntity saved = carts.save(assembler.toEntity(cart, existing));
            List<Event> pending = cart.pullEvents();
            events.produce("Cart", cart.getId().getValue(), pending);
            return assembler.toDomain(saved);
        });
    }
}
