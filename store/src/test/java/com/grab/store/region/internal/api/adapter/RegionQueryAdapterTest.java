package com.grab.store.region.internal.api.adapter;

import com.grab.store.region.internal.api.adapter.mapper.RegionQueryMapperImpl;
import com.grab.store.region.port.RegionQuery.RegionSlice;
import com.region.application.model.read.FindRegionSliceResult;
import com.region.application.model.read.RegionResult;
import com.region.application.port.inbound.FindRegionByCountryUseCase;
import com.region.application.port.inbound.FindRegionSliceUseCase;
import com.region.application.port.inbound.ListActiveCountryCodesUseCase;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegionQueryAdapterTest {
    @Test
    void find_existingRegion_returnsRegionSlice() {
        FindRegionSliceUseCase findRegion = query -> Optional.of(new FindRegionSliceResult("reg-1", "USD", "ACTIVE"));
        FindRegionByCountryUseCase findByCountry = query -> Optional.empty();
        ListActiveCountryCodesUseCase findCountries = query -> java.util.List.of();
        RegionQueryAdapter adapter = new RegionQueryAdapter(findRegion, findByCountry, findCountries, new RegionQueryMapperImpl());

        Optional<RegionSlice> slice = adapter.find("reg-1");

        assertThat(slice).isPresent();
        assertThat(slice.get().regionId()).isEqualTo("reg-1");
        assertThat(slice.get().currencyCode()).isEqualTo("USD");
        assertThat(slice.get().active()).isTrue();
    }

    @Test
    void findByCountry_existingCountry_returnsRegionSlice() {
        FindRegionSliceUseCase findRegion = query -> Optional.empty();
        FindRegionByCountryUseCase findByCountry = query -> Optional.of(
                new RegionResult("reg-sg", "Singapore", "SGD", "ACTIVE", Set.of("SG"))
        );
        ListActiveCountryCodesUseCase findCountries = query -> java.util.List.of();
        RegionQueryAdapter adapter = new RegionQueryAdapter(findRegion, findByCountry, findCountries, new RegionQueryMapperImpl());

        Optional<RegionSlice> slice = adapter.findByCountry("sg");

        assertThat(slice).isPresent();
        assertThat(slice.get().regionId()).isEqualTo("reg-sg");
        assertThat(slice.get().currencyCode()).isEqualTo("SGD");
    }
}
