package com.catalog.application.service;

import com.catalog.application.port.inbound.GetProductAuditUseCase;

import com.catalog.application.port.outbound.ProductAuditPort;
import com.catalog.domain.port.outbound.ProductRepository;
import com.grab.framework.id.IdGenerator;
import com.catalog.application.query.GetProductAuditQuery;
import com.catalog.application.query.GetProductAuditResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class GetProductAuditService implements GetProductAuditUseCase {

    private final ProductAuditPort productAuditPort;
    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;

        public GetProductAuditResult execute(GetProductAuditQuery query) {
        var productId = idGenerator.convertIdFrom(query.productId());
        var merchantId = idGenerator.convertIdFrom(query.merchantId());
        productRepository.find(productId, merchantId).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(query.productId())));
        return new GetProductAuditResult(
                query.productId(),
                productAuditPort.findProductAuditTrail(query.productId())
                        .stream()
                        .map(entry -> new GetProductAuditResult.Entry(
                                entry.eventType(),
                                entry.status(),
                                entry.occurredAt(),
                                entry.payload()
                        ))
                        .toList()
        );
    }
}
