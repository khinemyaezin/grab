package com.identity.application.port.inbound;

import com.identity.application.model.read.ResolveSellerAccessQuery;
import com.identity.application.model.read.SellerAccessResult;

public interface ResolveSellerAccessUseCase {
    SellerAccessResult execute(ResolveSellerAccessQuery query);
}
