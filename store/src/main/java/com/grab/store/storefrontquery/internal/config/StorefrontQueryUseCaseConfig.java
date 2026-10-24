package com.grab.store.storefrontquery.internal.config;

import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.discovery.application.port.outbound.DiscoveryRebuildRunRepository;
import com.grab.discovery.application.policy.DiscoveryProjectionPolicy;
import com.grab.framework.id.IdGenerator;
import com.grab.discovery.application.service.GetBuyableOfferBySlugService;
import com.grab.discovery.application.service.ProjectDiscoveryProductService;
import com.grab.discovery.application.service.RebuildDiscoveryProductsService;
import com.grab.discovery.application.service.SearchBuyableOffersService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorefrontQueryUseCaseConfig {

    @Bean
    public GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase(DiscoveryProductQueryPort queryPort) {
        return new GetBuyableOfferBySlugService(queryPort);
    }

    @Bean
    public ProjectDiscoveryProductUseCase projectDiscoveryProductUseCase(
            CatalogPort catalog,
            InventoryPort inventory,
            SalesChannelQueryPort salesChannels,
            DiscoveryProductRepository projections,
            DiscoveryProjectionPolicy policy
    ) {
        return new ProjectDiscoveryProductService(catalog, inventory, salesChannels, projections, policy);
    }

    @Bean
    public RebuildDiscoveryProductsUseCase rebuildDiscoveryProductsUseCase(
            CatalogPort catalog,
            DiscoveryProductRepository projections,
            DiscoveryRebuildRunRepository runs,
            ProjectDiscoveryProductUseCase projectUseCase,
            IdGenerator idGenerator
    ) {
        return new RebuildDiscoveryProductsService(catalog, projections, runs, projectUseCase, idGenerator);
    }

    @Bean
    public SearchBuyableOffersUseCase searchBuyableOffersUseCase(DiscoveryProductQueryPort queryPort) {
        return new SearchBuyableOffersService(queryPort);
    }

    @Bean
    public DiscoveryProjectionPolicy discoveryProjectionPolicy() {
        return new DiscoveryProjectionPolicy();
    }
}
