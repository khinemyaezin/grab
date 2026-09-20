package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.read.ListSalesChannelsQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import org.springframework.data.domain.Page;

public interface ListSalesChannelsUseCase {
    Page<SalesChannelResult> execute(ListSalesChannelsQuery query);
}
