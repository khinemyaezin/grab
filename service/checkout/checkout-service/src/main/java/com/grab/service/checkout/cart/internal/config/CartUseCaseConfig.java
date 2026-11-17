package com.grab.service.checkout.cart.internal.config;

import com.cart.application.port.inbound.AddItemToCartUseCase;
import com.cart.application.port.inbound.GetCurrentCartUseCase;
import com.cart.application.port.outbound.CartQueryPort;
import com.cart.application.service.AddItemToCartService;
import com.cart.application.service.GetCurrentCartService;
import com.cart.domain.port.outbound.CartRepository;
import com.grab.framework.id.IdGenerator;
import com.grab.rpc.adapter.RmiClientFactory;
import com.grab.service.checkout.cart.internal.adapter.outbound.message.CartPeerQueries;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CartUseCaseConfig {

    @Bean
    public AddItemToCartUseCase addItemToCartUseCase(
            CartRepository cartRepository,
            RmiClientFactory clients,
            IdGenerator idGenerator) {
        return command -> {
            var peers = new CartPeerQueries(clients, idGenerator, command.quantity(), command.regionId());
            var service = new AddItemToCartService(cartRepository, peers, peers, peers, peers, idGenerator);
            return service.execute(command);
        };
    }

    @Bean
    public GetCurrentCartUseCase getCurrentCartUseCase(CartQueryPort cartQueryPort) {
        return new GetCurrentCartService(cartQueryPort);
    }
}

