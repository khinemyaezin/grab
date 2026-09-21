package com.grab.store.cart.internal.config;

import com.cart.application.port.inbound.AddItemToCartUseCase;
import com.cart.application.port.inbound.GetCurrentCartUseCase;
import com.cart.application.port.outbound.CartQueryPort;
import com.cart.application.port.outbound.CatalogBuyabilityPort;
import com.cart.application.port.outbound.InventoryAvailabilityPort;
import com.cart.application.port.outbound.PricingQuotePort;
import com.cart.application.port.outbound.SalesChannelLookupPort;
import com.cart.application.service.AddItemToCartService;
import com.cart.application.service.GetCurrentCartService;
import com.cart.domain.port.outbound.CartRepository;
import com.grab.framework.id.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartUseCaseConfig {

    @Bean
    public AddItemToCartUseCase addItemToCartUseCase(
            CartRepository cartRepository,
            SalesChannelLookupPort salesChannelLookupPort,
            CatalogBuyabilityPort catalogBuyabilityPort,
            PricingQuotePort pricingQuotePort,
            InventoryAvailabilityPort inventoryAvailabilityPort,
            IdGenerator idGenerator
    ) {
        return new AddItemToCartService(
                cartRepository,
                salesChannelLookupPort,
                catalogBuyabilityPort,
                pricingQuotePort,
                inventoryAvailabilityPort,
                idGenerator
        );
    }

    @Bean
    public GetCurrentCartUseCase getCurrentCartUseCase(CartQueryPort cartQueryPort) {
        return new GetCurrentCartService(cartQueryPort);
    }
}
