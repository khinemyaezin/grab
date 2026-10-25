package com.grab.app.channeldiscovery.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.app.channeldiscovery.region.port.RegionQuery;
import com.grab.app.channeldiscovery.storefrontquery.internal.adapter.mapper.RegionContextMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionContextAdapterTest {
    @Mock
    private RegionQuery regions;
    @Mock
    private RegionContextMapper mapper;

    @Test
    void findByCountry_preservesMappedRegionFacts() {
        RegionQuery.RegionSlice slice = new RegionQuery.RegionSlice("region-sg", "USD", "INACTIVE");
        RegionContextPort.RegionView view = new RegionContextPort.RegionView(new CommonId("region-sg"), "USD", false);
        when(regions.findByCountry("SG")).thenReturn(Optional.of(slice));
        when(mapper.toRegionView(slice)).thenReturn(view);

        RegionContextAdapter adapter = new RegionContextAdapter(regions, mapper);

        assertThat(adapter.findByCountry("SG")).contains(view);
    }

    @Test
    void findById_convertsFrameworkIdToProviderId() {
        CommonId regionId = new CommonId("region-sg");
        RegionQuery.RegionSlice slice = new RegionQuery.RegionSlice("region-sg", "USD", "ACTIVE");
        RegionContextPort.RegionView view = new RegionContextPort.RegionView(regionId, "USD", true);
        when(regions.find("region-sg")).thenReturn(Optional.of(slice));
        when(mapper.toRegionView(slice)).thenReturn(view);

        RegionContextAdapter adapter = new RegionContextAdapter(regions, mapper);

        assertThat(adapter.findById(regionId)).contains(view);
    }
}
