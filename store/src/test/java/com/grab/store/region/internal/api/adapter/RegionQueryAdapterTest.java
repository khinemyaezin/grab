package com.grab.store.region.internal.api.adapter;

import com.grab.store.region.internal.api.adapter.mapper.RegionQueryMapper;
import com.grab.store.region.port.RegionQuery.RegionSlice;
import com.region.application.model.read.FindRegionSliceQuery;
import com.region.application.model.read.FindRegionSliceResult;
import com.region.application.port.inbound.FindRegionSliceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionQueryAdapterTest {

    @Mock
    private FindRegionSliceUseCase findRegionSliceUseCase;

    private RegionQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RegionQueryAdapter(findRegionSliceUseCase, new RegionQueryMapper());
    }

    @Test
    void find_existingRegion_returnsRegionSlice() {
        FindRegionSliceResult result = new FindRegionSliceResult("reg-1", "USD", "ACTIVE");
        when(findRegionSliceUseCase.execute(any(FindRegionSliceQuery.class)))
                .thenReturn(Optional.of(result));

        Optional<RegionSlice> slice = adapter.find("reg-1");

        assertThat(slice).isPresent();
        assertThat(slice.get().regionId()).isEqualTo("reg-1");
        assertThat(slice.get().currencyCode()).isEqualTo("USD");
        assertThat(slice.get().active()).isTrue();
    }
}
