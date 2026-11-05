package com.grab.app.checkout.cart.internal.config;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CartUseCaseConfig {

    @Bean
    public AddItemToCartUseCase addItemToCartUseCase(
            CartRepository cartRepository,
            @Autowired(required = false) SalesChannelLookupPort salesChannelLookupPort,
            @Autowired(required = false) CatalogBuyabilityPort catalogBuyabilityPort,
            @Autowired(required = false) PricingQuotePort pricingQuotePort,
            @Autowired(required = false) InventoryAvailabilityPort inventoryAvailabilityPort,
            IdGenerator idGenerator) {
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

