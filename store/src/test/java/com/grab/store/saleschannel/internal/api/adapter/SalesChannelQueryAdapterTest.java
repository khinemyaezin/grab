package com.grab.store.saleschannel.internal.api.adapter;

import com.grab.store.saleschannel.internal.api.adapter.mapper.SalesChannelQueryMapper;
import com.grab.store.saleschannel.port.SalesChannelQuery.SalesChannelSlice;
import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.model.read.FindSalesChannelSliceResult;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
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
class SalesChannelQueryAdapterTest {

    @Mock
    private FindSalesChannelSliceUseCase findSalesChannelSliceUseCase;

    private SalesChannelQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SalesChannelQueryAdapter(findSalesChannelSliceUseCase, new SalesChannelQueryMapper());
    }

    @Test
    void find_existingSalesChannel_returnsSalesChannelSlice() {
        FindSalesChannelSliceResult result = new FindSalesChannelSliceResult(
                "sc-1", "WEBSITE", "ENABLED", "m-1"
        );
        when(findSalesChannelSliceUseCase.execute(any(FindSalesChannelSliceQuery.class)))
                .thenReturn(Optional.of(result));

        Optional<SalesChannelSlice> slice = adapter.find("sc-1");

        assertThat(slice).isPresent();
        assertThat(slice.get().salesChannelId()).isEqualTo("sc-1");
        assertThat(slice.get().enabled()).isTrue();
        assertThat(slice.get().website()).isTrue();
        assertThat(slice.get().marketplace()).isFalse();
    }

    @Test
    void isEnabled_enabledChannel_returnsTrue() {
        FindSalesChannelSliceResult result = new FindSalesChannelSliceResult(
                "sc-1", "WEBSITE", "ENABLED", "m-1"
        );
        when(findSalesChannelSliceUseCase.execute(any(FindSalesChannelSliceQuery.class)))
                .thenReturn(Optional.of(result));

        boolean enabled = adapter.isEnabled("sc-1");

        assertThat(enabled).isTrue();
    }

    @Test
    void isEnabled_disabledOrMissingChannel_returnsFalse() {
        when(findSalesChannelSliceUseCase.execute(any(FindSalesChannelSliceQuery.class)))
                .thenReturn(Optional.empty());

        boolean enabled = adapter.isEnabled("sc-none");

        assertThat(enabled).isFalse();
    }
}
