package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.model.read.FindSalesChannelSliceResult;

import java.util.Optional;

public interface FindSalesChannelSliceUseCase {
    Optional<FindSalesChannelSliceResult> execute(FindSalesChannelSliceQuery query);
}
