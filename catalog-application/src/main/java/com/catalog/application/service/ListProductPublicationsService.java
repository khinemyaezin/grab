package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.port.inbound.ListProductPublicationsUseCase;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.model.read.ListProductPublicationsQuery;
import com.catalog.application.model.read.ProductPublicationItem;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListProductPublicationsService implements ListProductPublicationsUseCase {

    private final ProductQueryPort productQueryPort;

    public List<ProductPublicationItem> execute(ListProductPublicationsQuery query) {
        productQueryPort.findByIdAndMerchantId(query.productId(), query.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));
        return productQueryPort.findPublicationsByProductIds(List.of(query.productId())).stream()
                .map(view -> new ProductPublicationItem(view.variantId(), view.salesChannelId()))
                .toList();
    }
}
