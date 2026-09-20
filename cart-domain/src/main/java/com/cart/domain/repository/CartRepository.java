package com.cart.domain.repository;

import com.cart.domain.aggregate.Cart;
import com.cart.domain.enums.CartStatus;
import com.grab.framework.id.Id;

import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findById(Id id);

    Optional<Cart> findOpenByGuestToken(String guestToken, Id salesChannelId, Id regionId, CartStatus status);

    Cart save(Cart cart);
}
