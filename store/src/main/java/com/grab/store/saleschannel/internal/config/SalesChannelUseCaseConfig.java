package com.grab.store.saleschannel.internal.config;

import com.saleschannel.application.port.inbound.CheckSalesChannelUsableUseCase;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
import com.saleschannel.application.port.inbound.GetSalesChannelUseCase;
import com.saleschannel.application.port.inbound.ListSalesChannelsUseCase;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.application.service.CheckSalesChannelUsableService;
import com.saleschannel.application.service.FindSalesChannelSliceService;
import com.saleschannel.application.service.GetSalesChannelService;
import com.saleschannel.application.service.ListSalesChannelsService;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import com.saleschannel.domain.service.SalesChannelUniquenessService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SalesChannelUseCaseConfig {

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
}
