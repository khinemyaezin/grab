package com.merchant.application.port.outbound;

import com.merchant.application.model.read.MerchantScopeRevision;
import com.merchant.application.model.read.ResolveMerchantScopeRevisionQuery;

public interface MerchantScopeRevisionQueryPort {
    MerchantScopeRevision find(ResolveMerchantScopeRevisionQuery query);
}
