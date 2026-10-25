package com.grab.app.channeldiscovery.region.internal.config;

import com.region.application.port.inbound.FindRegionSliceUseCase;
import com.region.application.port.inbound.FindRegionByCountryUseCase;
import com.region.application.port.inbound.ListActiveCountryCodesUseCase;
import com.region.application.port.inbound.GetRegionUseCase;
import com.region.application.port.outbound.RegionQueryPort;
import com.region.application.service.FindRegionSliceService;
import com.region.application.service.FindRegionByCountryService;
import com.region.application.service.ListActiveCountryCodesService;
import com.region.application.service.GetRegionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegionUseCaseConfig {

    @Bean
    public FindRegionSliceUseCase findRegionSliceUseCase(RegionQueryPort regionQueryPort) {
        return new FindRegionSliceService(regionQueryPort);
    }

    @Bean
    public FindRegionByCountryUseCase findRegionByCountryUseCase(RegionQueryPort regionQueryPort) {
        return new FindRegionByCountryService(regionQueryPort);
    }

    @Bean
    public GetRegionUseCase getRegionUseCase(RegionQueryPort regionQueryPort) {
        return new GetRegionService(regionQueryPort);
    }

    @Bean
    public ListActiveCountryCodesUseCase listActiveCountryCodesUseCase(RegionQueryPort regionQueryPort) {
        return new ListActiveCountryCodesService(regionQueryPort);
    }
}
