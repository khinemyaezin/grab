package com.grab.app.channel.discovery.internal.config;

import com.grab.discovery.application.policy.ShoppingContextResolutionPolicy;
import com.grab.discovery.application.port.inbound.ResolveShoppingContextUseCase;
import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.discovery.application.port.outbound.SalesChannelRegionPort;
import com.grab.discovery.application.service.ResolveShoppingContextService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShoppingContextUseCaseConfig {
    @Bean
    public ShoppingContextResolutionPolicy shoppingContextResolutionPolicy() {
        return new ShoppingContextResolutionPolicy();
    }

    @Bean
    public ResolveShoppingContextUseCase resolveShoppingContextUseCase(
            SalesChannelPublishableKeyPort publishableKeys,
            SalesChannelRegionPort channelRegions,
            RegionContextPort regions,
            ShoppingContextResolutionPolicy policy
    ) {
        return new ResolveShoppingContextService(publishableKeys, channelRegions, regions, policy);
    }
}
