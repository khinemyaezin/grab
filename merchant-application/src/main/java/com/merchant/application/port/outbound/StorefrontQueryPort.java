package com.merchant.application.port.outbound;

import com.merchant.application.model.read.StorefrontQueryCriteria;
import com.merchant.application.model.read.StorefrontView;

import java.util.List;
import java.util.Optional;

public interface StorefrontQueryPort {
    List<StorefrontView> list(StorefrontQueryCriteria criteria);

    Optional<StorefrontView> findById(String storefrontId);
}
