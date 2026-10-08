package com.grab.store.saleschannel.internal.api.adapter.mapper;

import com.grab.store.saleschannel.port.SalesChannelQuery.SalesChannelSlice;
import com.saleschannel.application.model.read.FindSalesChannelSliceResult;
import org.springframework.stereotype.Component;

@Component
public class SalesChannelQueryMapper {

    public SalesChannelSlice toSlice(FindSalesChannelSliceResult slice) {
        return new SalesChannelSlice(
                slice.salesChannelId(),
                slice.type(),
                slice.status(),
                slice.merchantId()
        );
    }
}
