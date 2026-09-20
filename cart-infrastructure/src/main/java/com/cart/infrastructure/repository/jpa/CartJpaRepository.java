package com.cart.infrastructure.repository.jpa;

import com.cart.domain.enums.CartStatus;
import com.cart.infrastructure.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUuid(String uuid);

    Optional<CartEntity> findByGuestTokenAndSalesChannelIdAndRegionIdAndStatus(
            String guestToken,
            String salesChannelId,
            String regionId,
            CartStatus status
    );
}
