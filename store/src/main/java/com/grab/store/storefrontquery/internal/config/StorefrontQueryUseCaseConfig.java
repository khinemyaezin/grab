package com.grab.store.storefrontquery.internal.config;

import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.discovery.application.port.outbound.SalesChannelLookupPort;
import com.grab.discovery.application.service.GetBuyableOfferBySlugService;
import com.grab.discovery.application.service.ProjectDiscoveryProductService;
import com.grab.discovery.application.service.SearchBuyableOffersService;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.discovery.application.service.RebuildDiscoveryProductsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorefrontQueryUseCaseConfig {

    @Bean
    public SearchBuyableOffersUseCase searchBuyableOffersUseCase(
            DiscoveryProductQueryPort products,
            SalesChannelLookupPort salesChannels
    ) {
        return new SearchBuyableOffersService(products, salesChannels);
    }

    @Bean
    public GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase(
            DiscoveryProductQueryPort products,
            SalesChannelLookupPort salesChannels
    ) {
        return new GetBuyableOfferBySlugService(products, salesChannels);
    }


    @Bean
    public ProjectDiscoveryProductUseCase projectDiscoveryProductUseCase(
            DiscoveryProductRepository products,
            CatalogPort catalog,
            PricingPort pricing,
            InventoryPort inventory,
            SalesChannelLookupPort salesChannels,
            @Value("${storefrontquery.display-currency:MMK}") String displayCurrency
    ) {
        return new ProjectDiscoveryProductService(products, catalog, pricing, inventory, salesChannels, displayCurrency);
    }

    @Bean
    public RebuildDiscoveryProductsUseCase rebuildDiscoveryProductsUseCase(
            CatalogPort catalog,
            ProjectDiscoveryProductUseCase projectDiscoveryProductUseCase
    ) {
        return new RebuildDiscoveryProductsService(catalog, projectDiscoveryProductUseCase);
    }

}
