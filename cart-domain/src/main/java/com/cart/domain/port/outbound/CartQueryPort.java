package com.cart.domain.port.outbound;

import com.cart.domain.enums.CartStatus;
import com.cart.domain.readmodel.CartView;

import java.util.Optional;

public interface CartQueryPort {
    Optional<CartView> findOpenByGuestToken(
            String guestToken,
            String salesChannelId,
            String regionId,
            CartStatus status
    );
}
