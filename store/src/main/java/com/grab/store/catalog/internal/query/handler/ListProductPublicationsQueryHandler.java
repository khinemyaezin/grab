package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.ListProductPublicationsQuery;
import com.grab.store.catalog.internal.query.ProductPublicationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListProductPublicationsQueryHandler
        implements QueryHandler<ListProductPublicationsQuery, List<ProductPublicationItem>> {

    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;
    private final IdGenerator idGenerator;

    @Override
    @CatalogReadTransactional
    public List<ProductPublicationItem> handle(ListProductPublicationsQuery query) {
        productRepository.find(
                        idGenerator.convertIdFrom(query.productId()),
                        idGenerator.convertIdFrom(query.merchantId()))
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));
        return productQueryRepository.findPublicationsByProductIds(List.of(query.productId())).stream()
                .map(view -> new ProductPublicationItem(view.variantId(), view.salesChannelId()))
                .toList();
    }

    @Override
    public Class<ListProductPublicationsQuery> getQueryType() {
        return ListProductPublicationsQuery.class;
    }
}
