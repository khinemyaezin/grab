package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CheckProductPublishedToChannelQuery;

public interface CheckProductPublishedToChannelUseCase {
    boolean execute(CheckProductPublishedToChannelQuery query);
}
