package com.cart.application.port.outbound;

import com.cart.application.model.read.CartView;
import com.cart.domain.enums.CartStatus;

import java.util.Optional;

public interface CartQueryPort {
    Optional<CartView> findOpenByGuestToken(
            String guestToken,
            String salesChannelId,
            String regionId,
            CartStatus status
    );
}
