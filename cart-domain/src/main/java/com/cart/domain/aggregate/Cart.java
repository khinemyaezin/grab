package com.cart.domain.aggregate;

import com.cart.domain.entity.CartLine;
import com.cart.domain.enums.CartStatus;
import com.cart.domain.event.CartCreatedEvent;
import com.cart.domain.event.CartLineAddedEvent;
import com.cart.domain.exception.CartDomainError;
import com.cart.domain.exception.CartDomainException;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public class Cart extends AggregateRoot<Id> {
    private final Id salesChannelId;
    private final String channelType;
    private final Id regionId;
    private final String currencyCode;
    private final String guestToken;
    private CartStatus status;
    private final List<CartLine> lines;
    private final Instant createdAt;
    private Instant updatedAt;

    public Cart(
            Id id,
            Id salesChannelId,
            String channelType,
            Id regionId,
            String currencyCode,
            String guestToken,
            CartStatus status,
            List<CartLine> lines,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id);
        this.salesChannelId = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        this.channelType = Objects.requireNonNull(channelType, "channelType is required");
        this.regionId = regionId;
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode is required");
        this.guestToken = Objects.requireNonNull(guestToken, "guestToken is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Cart create(
            Id id,
            Id salesChannelId,
            String channelType,
            Id regionId,
            String currencyCode,
            String guestToken,
            Instant now
    ) {
        Cart cart = new Cart(
                id,
                salesChannelId,
                channelType,
                regionId,
                currencyCode,
                guestToken,
                CartStatus.OPEN,
                new ArrayList<>(),
                now,
                now
        );
        cart.addEvent(new CartCreatedEvent(id.getValue(), salesChannelId.getValue(), guestToken, now));
        return cart;
    }

    public CartLine addOrIncrease(
            Id lineId,
            Id variantId,
            Id productId,
            Id sellerId,
            String title,
            String sku,
            BigDecimal unitPrice,
            int quantity,
            Instant now
    ) {
        requireOpen();
        if (quantity <= 0) {
            throw new CartDomainException(new CartDomainError.InvalidQuantity(quantity), "Quantity must be positive");
        }
        if ("WEBSITE".equals(channelType) && !lines.isEmpty()) {
            Id existingSeller = lines.getFirst().getSellerId();
            if (!existingSeller.equals(sellerId)) {
                throw new CartDomainException(
                        new CartDomainError.MixedSeller(existingSeller.getValue(), sellerId.getValue()),
                        "Website carts stay single-seller"
                );
            }
        }
        Optional<CartLine> existing = lines.stream()
                .filter(line -> line.getVariantId().equals(variantId))
                .findFirst();
        CartLine line;
        if (existing.isPresent()) {
            line = existing.get();
            line.replaceQuantityAndPrice(line.getQuantity() + quantity, unitPrice);
        } else {
            line = new CartLine(lineId, variantId, productId, sellerId, title, sku, unitPrice, quantity);
            lines.add(line);
        }
        this.updatedAt = now;
        addEvent(new CartLineAddedEvent(getId().getValue(), variantId.getValue(), line.getQuantity(), unitPrice, now));
        return line;
    }

    private void requireOpen() {
        if (status != CartStatus.OPEN) {
            throw new CartDomainException(
                    new CartDomainError.CartNotOpen(getId().getValue(), status.name()),
                    "Cart is not open"
            );
        }
    }
}
