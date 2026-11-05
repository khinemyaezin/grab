package com.grab.app.channel.discovery.internal.config;

import com.grab.discovery.application.policy.ContextualOfferPolicy;
import com.grab.discovery.application.policy.DiscoveryProjectionPolicy;
import com.grab.discovery.application.policy.ShoppingContextResolutionPolicy;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.discovery.application.port.inbound.ResolveShoppingContextUseCase;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.discovery.application.port.outbound.DiscoveryRebuildRunRepository;
import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.discovery.application.port.outbound.SalesChannelRegionPort;
import com.grab.discovery.application.service.GetBuyableOfferBySlugService;
import com.grab.discovery.application.service.ProjectDiscoveryProductService;
import com.grab.discovery.application.service.RebuildDiscoveryProductsService;
import com.grab.discovery.application.service.ResolveShoppingContextService;
import com.grab.discovery.application.service.SearchBuyableOffersService;
import com.grab.framework.id.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class DiscoveryUseCaseConfig {

    @Bean
    public ContextualOfferPolicy contextualOfferPolicy() {
        return new ContextualOfferPolicy();
    }

    @Bean
    public DiscoveryProjectionPolicy discoveryProjectionPolicy() {
        return new DiscoveryProjectionPolicy();
    }

    @Bean
    public ShoppingContextResolutionPolicy shoppingContextResolutionPolicy() {
        return new ShoppingContextResolutionPolicy();
    }

    @Bean
    public GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase(
            DiscoveryProductQueryPort queryPort,
            @Autowired(required = false) PricingPort pricingPort,
            ContextualOfferPolicy contextualOfferPolicy
    ) {
        return new GetBuyableOfferBySlugService(queryPort, pricingPort, contextualOfferPolicy);
    }

    @Bean
    public ProjectDiscoveryProductUseCase projectDiscoveryProductUseCase(
            @Autowired(required = false) CatalogPort catalog,
            @Autowired(required = false) InventoryPort inventory,
            @Autowired(required = false) SalesChannelQueryPort salesChannels,
            DiscoveryProductRepository projections,
            DiscoveryProjectionPolicy policy
    ) {
        return new ProjectDiscoveryProductService(catalog, inventory, salesChannels, projections, policy);
    }

    @Bean
    public RebuildDiscoveryProductsUseCase rebuildDiscoveryProductsUseCase(
            @Autowired(required = false) CatalogPort catalog,
            DiscoveryProductRepository projections,
            DiscoveryRebuildRunRepository runs,
            ProjectDiscoveryProductUseCase projectUseCase,
            IdGenerator idGenerator
    ) {
        return new RebuildDiscoveryProductsService(catalog, projections, runs, projectUseCase, idGenerator);
    }

    @Bean
    public SearchBuyableOffersUseCase searchBuyableOffersUseCase(
            DiscoveryProductQueryPort queryPort,
            @Autowired(required = false) PricingPort pricingPort,
            ContextualOfferPolicy contextualOfferPolicy
    ) {
        return new SearchBuyableOffersService(queryPort, pricingPort, contextualOfferPolicy);
    }

    @Bean
    public ResolveShoppingContextUseCase resolveShoppingContextUseCase(
            @Autowired(required = false) SalesChannelPublishableKeyPort publishableKeys,
            @Autowired(required = false) SalesChannelRegionPort channelRegions,
            @Autowired(required = false) RegionContextPort regions,
            ShoppingContextResolutionPolicy policy
    ) {
        return new ResolveShoppingContextService(publishableKeys, channelRegions, regions, policy);
    }
}

