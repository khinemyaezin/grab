package com.identity.application.port.inbound;

import com.identity.application.model.read.ResolveSellerAuthenticationQuery;
import com.identity.application.model.read.SellerAuthenticationResult;

public interface ResolveSellerAuthenticationUseCase {
    SellerAuthenticationResult execute(ResolveSellerAuthenticationQuery query);
}
