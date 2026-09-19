package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.CheckProductPublishableQuery;
import com.grab.store.catalog.internal.query.CheckProductPublishableResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckProductPublishableQueryHandler
        implements QueryHandler<CheckProductPublishableQuery, CheckProductPublishableResult> {

    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;

    @Override
    @CatalogReadTransactional
    public CheckProductPublishableResult handle(CheckProductPublishableQuery query) {
        return productRepository.find(idGenerator.convertIdFrom(query.productId()))
                .map(product -> toResult(product, query.merchantId()))
                .orElseGet(CheckProductPublishableResult::missing);
    }

    @Override
    public Class<CheckProductPublishableQuery> getQueryType() {
        return CheckProductPublishableQuery.class;
    }

    private CheckProductPublishableResult toResult(Product product, String merchantId) {
        boolean owned = product.getMerchantId() != null && product.getMerchantId().getValue().equals(merchantId);
        boolean active = product.getStatus() == ProductStatus.ACTIVE;
        return new CheckProductPublishableResult(true, owned, active);
    }
}
