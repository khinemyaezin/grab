package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.port.inbound.GetProductAuditUseCase;
import com.catalog.application.port.outbound.ProductAuditPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.model.read.GetProductAuditQuery;
import com.catalog.application.model.read.GetProductAuditResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetProductAuditService implements GetProductAuditUseCase {

    private final ProductAuditPort productAuditPort;
    private final ProductQueryPort productQueryPort;

    public GetProductAuditResult execute(GetProductAuditQuery query) {
        productQueryPort.findByIdAndMerchantId(query.productId(), query.merchantId()).orElseThrow(() ->
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
