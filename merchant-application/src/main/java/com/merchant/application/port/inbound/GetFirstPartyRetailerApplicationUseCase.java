package com.merchant.application.port.inbound;

import com.merchant.application.model.read.GetFirstPartyRetailerApplicationQuery;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationResult;

public interface GetFirstPartyRetailerApplicationUseCase {
    GetFirstPartyRetailerApplicationResult execute(GetFirstPartyRetailerApplicationQuery query);
}
