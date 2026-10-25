package com.grab.app.channeldiscovery.saleschannel.internal.api.adapter.mapper;

import com.grab.app.channeldiscovery.saleschannel.port.SalesChannelPort.SalesChannelSlice;
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
