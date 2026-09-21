package com.catalog.application.port.inbound;

import com.catalog.application.model.read.CheckProductPublishableQuery;
import com.catalog.application.model.read.CheckProductPublishableResult;

public interface CheckProductPublishableUseCase {
    CheckProductPublishableResult execute(CheckProductPublishableQuery query);
}
