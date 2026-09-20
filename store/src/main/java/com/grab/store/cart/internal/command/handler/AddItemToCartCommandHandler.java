package com.grab.store.cart.internal.command.handler;

import com.cart.domain.aggregate.Cart;
import com.cart.domain.entity.CartLine;
import com.cart.domain.enums.CartStatus;
import com.cart.domain.exception.CartDomainError;
import com.cart.domain.exception.CartDomainException;
import com.cart.domain.repository.CartRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.cart.internal.command.AddItemToCartCommand;
import com.grab.store.cart.internal.command.CartResult;
import com.grab.store.cart.internal.config.CartTransactional;
import com.grab.store.catalog.query.CatalogBuyabilityQueryPort;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import com.grab.store.pricing.query.PricingQuoteQueryPort;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddItemToCartCommandHandler implements CommandHandler<AddItemToCartCommand, CartResult> {
    private final CartRepository carts;
    private final SalesChannelQueryPort salesChannels;
    private final CatalogBuyabilityQueryPort catalog;
    private final PricingQuoteQueryPort pricing;
    private final InventoryAvailabilityQueryPort inventory;
    private final IdGenerator ids;

    @Override
    @CartTransactional
    public CartResult handle(AddItemToCartCommand command) {
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

        SalesChannelQueryPort.SalesChannelSlice channel = salesChannels.find(command.salesChannelId())
                .filter(SalesChannelQueryPort.SalesChannelSlice::enabled)
                .orElseThrow(() -> new CartDomainException(
                        new CartDomainError.ChannelDisabled(command.salesChannelId()),
                        "Sales channel is disabled"
                ));

        CatalogBuyabilityQueryPort.CatalogVariantSlice variant = catalog.findVariant(command.variantId())
                .orElseThrow(() -> unpublished(command));
        if (!variant.active() || !catalog.isPublished(command.variantId(), command.salesChannelId())) {
            throw unpublished(command);
        }

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
                channel.type() == null ? "MARKETPLACE" : channel.type(),
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

        PricingQuoteQueryPort.QuotedPrice quote = pricing.quote(
                command.variantId(),
                cart.getCurrencyCode(),
                requestedTotal,
                command.salesChannelId()
        ).orElseThrow(() -> new CartDomainException(
                new CartDomainError.NotPriced(command.variantId()),
                "Variant is not priced"
        ));

        InventoryAvailabilityQueryPort.Availability availability = inventory.available(variant.sku(), command.salesChannelId());
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

    @Override
    public Class<AddItemToCartCommand> getCommandType() {
        return AddItemToCartCommand.class;
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
