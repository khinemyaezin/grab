package com.cart.application.service;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;
import com.cart.application.port.inbound.AddItemToCartUseCase;
import com.cart.application.port.outbound.CatalogBuyabilityPort;
import com.cart.application.port.outbound.InventoryAvailabilityPort;
import com.cart.application.port.outbound.PricingQuotePort;
import com.cart.application.port.outbound.SalesChannelLookupPort;
import com.cart.domain.aggregate.Cart;
import com.cart.domain.entity.CartLine;
import com.cart.domain.enums.CartStatus;
import com.cart.domain.exception.CartDomainError;
import com.cart.domain.exception.CartDomainException;
import com.cart.domain.port.outbound.CartRepository;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class AddItemToCartService implements AddItemToCartUseCase {
    private final CartRepository carts;
    private final SalesChannelLookupPort salesChannels;
    private final CatalogBuyabilityPort catalog;
    private final PricingQuotePort pricing;
    private final InventoryAvailabilityPort inventory;
    private final IdGenerator ids;

    @Override
    public CartResult execute(AddItemToCartCommand command) {
        if (command.quantity() <= 0) {
            throw new CartDomainException(
                    new CartDomainError.InvalidQuantity(command.quantity()),
                    "Quantity must be positive"
            );
        }
        if (command.salesChannelId() == null || command.salesChannelId().isBlank()) {
            throw new CartDomainException(
                    new CartDomainError.ChannelDisabled(""),
                    "Sales channel is disabled"
            );
        }
        if (command.variantId() == null || command.variantId().isBlank()) {
            throw unpublished(command);
        }

        SalesChannelLookupPort.ChannelSnapshot channel = salesChannels.findEnabled(command.salesChannelId())
                .orElseThrow(() -> new CartDomainException(
                        new CartDomainError.ChannelDisabled(command.salesChannelId()),
                        "Sales channel is disabled"
                ));

        CatalogBuyabilityPort.BuyableVariant variant = catalog.findPublished(command.variantId(), command.salesChannelId())
                .orElseThrow(() -> unpublished(command));

        String guestToken = blank(command.guestToken()) ? UUID.randomUUID().toString() : command.guestToken();
        String regionId = blank(command.regionId()) ? "default" : command.regionId();
        String currency = blank(command.currencyCode()) ? "MMK" : command.currencyCode();

        Cart cart = carts.findOpenByGuestToken(
                guestToken,
                ids.convertIdFrom(command.salesChannelId()),
                ids.convertIdFrom(regionId),
                CartStatus.OPEN
        ).orElseGet(() -> Cart.create(
                ids.generateId(),
                ids.convertIdFrom(command.salesChannelId()),
                channel.type(),
                ids.convertIdFrom(regionId),
                currency,
                guestToken,
                Instant.now()
        ));

        int existingQty = cart.getLines().stream()
                .filter(line -> line.getVariantId().getValue().equals(command.variantId()))
                .mapToInt(CartLine::getQuantity)
                .findFirst()
                .orElse(0);
        int requestedTotal = existingQty + command.quantity();

        PricingQuotePort.QuotedUnitPrice quote = pricing.quote(
                command.variantId(),
                cart.getCurrencyCode(),
                requestedTotal,
                command.salesChannelId()
        ).orElseThrow(() -> new CartDomainException(
                new CartDomainError.NotPriced(command.variantId()),
                "Variant is not priced"
        ));

        InventoryAvailabilityPort.Availability availability = inventory.available(variant.sku(), command.salesChannelId());
        boolean untracked = variant.untracked() || availability.untracked();
        if (!untracked && availability.availableQty() < requestedTotal) {
            throw new CartDomainException(
                    new CartDomainError.InsufficientStock(variant.sku(), requestedTotal, availability.availableQty()),
                    "Insufficient stock"
            );
        }

        cart.addOrIncrease(
                ids.generateId(),
                ids.convertIdFrom(command.variantId()),
                variant.productId() == null ? null : ids.convertIdFrom(variant.productId()),
                ids.convertIdFrom(variant.sellerId()),
                variant.title(),
                variant.sku(),
                quote.amount(),
                command.quantity(),
                Instant.now()
        );
        Cart saved = carts.save(cart);
        boolean priceAdjusted = command.displayedAmount() != null
                && command.displayedAmount().compareTo(quote.amount()) != 0;
        return CartResult.from(saved, priceAdjusted);
    }

    private static CartDomainException unpublished(AddItemToCartCommand command) {
        return new CartDomainException(
                new CartDomainError.NotPublished(
                        command.variantId() == null ? "" : command.variantId(),
                        command.salesChannelId() == null ? "" : command.salesChannelId()
                ),
                "Variant is not published to this channel"
        );
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
