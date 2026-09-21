package com.identity.application.port.outbound;

import com.identity.application.model.read.MerchantView;

import java.util.Collection;
import java.util.List;

public interface MerchantViewQueryPort {
    List<MerchantView> findAllByScopeIdIn(Collection<String> scopeIds);
}
