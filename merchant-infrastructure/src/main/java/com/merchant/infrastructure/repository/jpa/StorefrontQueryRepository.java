package com.merchant.infrastructure.repository.jpa;

import com.merchant.infrastructure.specification.jpa.StorefrontQueryCriteria;
import com.merchant.infrastructure.view.StorefrontView;

import java.util.List;

public interface StorefrontQueryRepository {
    List<StorefrontView> list(StorefrontQueryCriteria criteria);
}
