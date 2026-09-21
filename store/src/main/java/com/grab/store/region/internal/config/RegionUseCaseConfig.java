package com.grab.store.region.internal.config;

import com.region.application.port.inbound.FindRegionSliceUseCase;
import com.region.application.port.inbound.GetRegionUseCase;
import com.region.application.port.outbound.RegionQueryPort;
import com.region.application.service.FindRegionSliceService;
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
    public GetRegionUseCase getRegionUseCase(RegionQueryPort regionQueryPort) {
        return new GetRegionService(regionQueryPort);
    }
}
