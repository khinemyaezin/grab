package com.catalog.application.port.inbound;

import com.catalog.application.query.CheckProductPublishableQuery;
import com.catalog.application.query.CheckProductPublishableResult;

public interface CheckProductPublishableUseCase {
    CheckProductPublishableResult execute(CheckProductPublishableQuery query);
}
