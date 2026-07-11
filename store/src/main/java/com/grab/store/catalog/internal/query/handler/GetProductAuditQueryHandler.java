package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CatalogOutboxEventJpaRepo;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.GetProductAuditQuery;
import com.grab.store.catalog.internal.query.GetProductAuditResult;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProductAuditQueryHandler implements QueryHandler<GetProductAuditQuery, GetProductAuditResult> {

    private final CatalogOutboxEventJpaRepo outboxEventJpaRepo;
    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;

    @Override
    @CatalogReadTransactional
    public GetProductAuditResult handle(GetProductAuditQuery query) {
        var productId = idGenerator.convertIdFrom(query.productId());
        var merchantId = idGenerator.convertIdFrom(query.merchantId());
        productRepository.find(productId, merchantId).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(query.productId())));
        return new GetProductAuditResult(
                query.productId(),
                outboxEventJpaRepo.findByAggregateTypeAndAggregateIdOrderByOccurredAtDesc("Product", query.productId())
                        .stream()
                        .map(event -> new GetProductAuditResult.Entry(
                                event.getEventType(),
                                event.getStatus().name(),
                                event.getOccurredAt(),
                                event.getPayload()
                        ))
                        .toList()
        );
    }

    @Override
    public Class<GetProductAuditQuery> getQueryType() {
        return GetProductAuditQuery.class;
    }
}
