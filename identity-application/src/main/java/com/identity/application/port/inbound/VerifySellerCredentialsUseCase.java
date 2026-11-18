package com.identity.application.port.inbound;

import com.identity.application.model.read.VerifiedSeller;
import com.identity.application.model.read.VerifySellerCredentialsQuery;

public interface VerifySellerCredentialsUseCase {
    VerifiedSeller execute(VerifySellerCredentialsQuery query);
}
