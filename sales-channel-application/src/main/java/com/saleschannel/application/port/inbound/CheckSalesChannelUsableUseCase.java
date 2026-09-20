package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.read.CheckSalesChannelUsableQuery;
import com.saleschannel.application.model.read.CheckSalesChannelUsableResult;

public interface CheckSalesChannelUsableUseCase {
    CheckSalesChannelUsableResult execute(CheckSalesChannelUsableQuery query);
}
