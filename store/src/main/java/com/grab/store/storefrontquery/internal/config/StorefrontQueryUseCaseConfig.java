package com.grab.store.storefrontquery.internal.config;

import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import com.grab.discovery.application.service.GetBuyableOfferBySlugService;
import com.grab.discovery.application.service.ProjectDiscoveryProductService;
import com.grab.discovery.application.service.RebuildDiscoveryProductsService;
import com.grab.discovery.application.service.SearchBuyableOffersService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorefrontQueryUseCaseConfig {

    @Bean
    public GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase() {
        return new GetBuyableOfferBySlugService();
    }

    @Bean
    public ProjectDiscoveryProductUseCase projectDiscoveryProductUseCase() {
        return new ProjectDiscoveryProductService();
    }

    @Bean
    public RebuildDiscoveryProductsUseCase rebuildDiscoveryProductsUseCase() {
        return new RebuildDiscoveryProductsService();
    }

    @Bean
    public SearchBuyableOffersUseCase searchBuyableOffersUseCase() {
        return new SearchBuyableOffersService();
    }
}
