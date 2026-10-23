package com.grab.store.saleschannel.internal.config;

import com.saleschannel.application.port.inbound.CheckSalesChannelUsableUseCase;
import com.saleschannel.application.port.inbound.EnsureMarketplaceChannelUseCase;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
import com.saleschannel.application.port.inbound.GetSalesChannelUseCase;
import com.saleschannel.application.port.inbound.ListSalesChannelsUseCase;
import com.saleschannel.application.port.inbound.ProvisionMerchantWebsiteChannelUseCase;
import com.saleschannel.application.port.inbound.ApplySalesChannelRegionBindingsUseCase;
import com.saleschannel.application.port.inbound.FindDefaultSalesChannelRegionUseCase;
import com.saleschannel.application.port.inbound.ListEnabledSalesChannelRegionsUseCase;
import com.saleschannel.application.port.inbound.ServesSalesChannelRegionUseCase;
import com.saleschannel.application.port.inbound.CreatePublishableKeyUseCase;
import com.saleschannel.application.port.inbound.ListPublishableKeysUseCase;
import com.saleschannel.application.port.inbound.ResolvePublishableKeyUseCase;
import com.saleschannel.application.port.inbound.RevokePublishableKeyUseCase;
import com.saleschannel.application.port.outbound.ChannelRegionLookupPort;
import com.saleschannel.application.port.outbound.RegionStatusPort;
import com.saleschannel.application.port.outbound.PrimaryMarketRegionPort;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.application.port.outbound.PublishableKeyQueryPort;
import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort;
import com.saleschannel.application.service.CheckSalesChannelUsableService;
import com.saleschannel.application.service.EnsureMarketplaceChannelService;
import com.saleschannel.application.service.FindSalesChannelSliceService;
import com.saleschannel.application.service.GetSalesChannelService;
import com.saleschannel.application.service.ListSalesChannelsService;
import com.saleschannel.application.service.ProvisionMerchantWebsiteChannelService;
import com.saleschannel.application.service.ApplySalesChannelRegionBindingsService;
import com.saleschannel.application.service.FindDefaultSalesChannelRegionService;
import com.saleschannel.application.service.ListEnabledSalesChannelRegionsService;
import com.saleschannel.application.service.ServesSalesChannelRegionService;
import com.saleschannel.application.service.CreatePublishableKeyService;
import com.saleschannel.application.service.ListPublishableKeysService;
import com.saleschannel.application.service.ResolvePublishableKeyService;
import com.saleschannel.application.service.RevokePublishableKeyService;
import com.grab.framework.id.IdGenerator;
import com.saleschannel.domain.policy.PublishableKeyManagementPolicy;
import com.saleschannel.domain.port.outbound.PublishableKeyRepository;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import com.saleschannel.domain.service.SalesChannelUniquenessService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class SalesChannelUseCaseConfig {

    @Bean
    public Clock salesChannelClock() {
        return Clock.systemUTC();
    }

    @Bean
    public SalesChannelUniquenessService salesChannelUniquenessService(SalesChannelRepository salesChannelRepository) {
        return new SalesChannelUniquenessService(salesChannelRepository);
    }

    @Bean
    public GetSalesChannelUseCase getSalesChannelUseCase(SalesChannelQueryPort salesChannelQueryPort) {
        return new GetSalesChannelService(salesChannelQueryPort);
    }

    @Bean
    public ListSalesChannelsUseCase listSalesChannelsUseCase(SalesChannelQueryPort salesChannelQueryPort) {
        return new ListSalesChannelsService(salesChannelQueryPort);
    }

    @Bean
    public CheckSalesChannelUsableUseCase checkSalesChannelUsableUseCase(SalesChannelQueryPort salesChannelQueryPort) {
        return new CheckSalesChannelUsableService(salesChannelQueryPort);
    }

    @Bean
    public FindSalesChannelSliceUseCase findSalesChannelSliceUseCase(SalesChannelQueryPort salesChannelQueryPort) {
        return new FindSalesChannelSliceService(salesChannelQueryPort);
    }

    @Bean
    public EnsureMarketplaceChannelUseCase ensureMarketplaceChannelUseCase(
            SalesChannelRepository salesChannelRepository,
            IdGenerator idGenerator
    ) {
        return new EnsureMarketplaceChannelService(salesChannelRepository, idGenerator);
    }

    @Bean
    public ProvisionMerchantWebsiteChannelUseCase provisionMerchantWebsiteChannelUseCase(
            SalesChannelRepository salesChannelRepository,
            IdGenerator idGenerator,
            PrimaryMarketRegionPort primaryMarketRegionPort
    ) {
        return new ProvisionMerchantWebsiteChannelService(salesChannelRepository, idGenerator, primaryMarketRegionPort);
    }

    @Bean
    public ServesSalesChannelRegionUseCase servesSalesChannelRegionUseCase(
            ChannelRegionLookupPort channelRegionLookupPort
    ) {
        return new ServesSalesChannelRegionService(channelRegionLookupPort);
    }

    @Bean
    public FindDefaultSalesChannelRegionUseCase findDefaultSalesChannelRegionUseCase(
            ChannelRegionLookupPort channelRegionLookupPort
    ) {
        return new FindDefaultSalesChannelRegionService(channelRegionLookupPort);
    }

    @Bean
    public ListEnabledSalesChannelRegionsUseCase listEnabledSalesChannelRegionsUseCase(
            ChannelRegionLookupPort channelRegionLookupPort
    ) {
        return new ListEnabledSalesChannelRegionsService(channelRegionLookupPort);
    }

    @Bean
    public ApplySalesChannelRegionBindingsUseCase applySalesChannelRegionBindingsUseCase(
            SalesChannelRepository salesChannelRepository,
            RegionStatusPort regionStatusPort
    ) {
        return new ApplySalesChannelRegionBindingsService(salesChannelRepository, regionStatusPort);
    }

    @Bean
    public CreatePublishableKeyUseCase createPublishableKeyUseCase(
            SalesChannelRepository salesChannelRepository,
            com.saleschannel.domain.port.outbound.PublishableKeyRepository publishableKeyRepository,
            PublishableTokenGeneratorPort tokenGenerator,
            IdGenerator idGenerator,
            Clock salesChannelClock,
            PublishableKeyManagementPolicy managementPolicy
    ) {
        return new CreatePublishableKeyService(
                salesChannelRepository, publishableKeyRepository, tokenGenerator, idGenerator,
                salesChannelClock, managementPolicy);
    }

    @Bean
    public RevokePublishableKeyUseCase revokePublishableKeyUseCase(
            SalesChannelRepository salesChannelRepository,
            PublishableKeyRepository publishableKeyRepository,
            Clock salesChannelClock,
            com.saleschannel.domain.policy.PublishableKeyManagementPolicy managementPolicy
    ) {
        return new RevokePublishableKeyService(
                salesChannelRepository, publishableKeyRepository, salesChannelClock, managementPolicy);
    }

    @Bean
    public ListPublishableKeysUseCase listPublishableKeysUseCase(
            SalesChannelQueryPort salesChannelQueryPort,
            PublishableKeyQueryPort publishableKeyQueryPort,
            IdGenerator idGenerator,
            PublishableKeyManagementPolicy managementPolicy
    ) {
        return new ListPublishableKeysService(
                salesChannelQueryPort, publishableKeyQueryPort, idGenerator, managementPolicy);
    }

    @Bean
    public ResolvePublishableKeyUseCase resolvePublishableKeyUseCase(
            PublishableKeyQueryPort publishableKeyQueryPort,
            PublishableTokenGeneratorPort tokenGenerator
    ) {
        return new ResolvePublishableKeyService(publishableKeyQueryPort, tokenGenerator);
    }
}
