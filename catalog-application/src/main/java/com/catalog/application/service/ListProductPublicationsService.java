package com.catalog.application.service;

import com.catalog.application.port.inbound.ListProductPublicationsUseCase;

import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.domain.port.outbound.ProductRepository;
import com.grab.framework.id.IdGenerator;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.ListProductPublicationsQuery;
import com.catalog.application.query.ProductPublicationItem;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListProductPublicationsService implements ListProductPublicationsUseCase {

    private final ProductRepository productRepository;
    private final ProductQueryPort productQueryRepository;
    private final IdGenerator idGenerator;

        public List<ProductPublicationItem> execute(ListProductPublicationsQuery query) {
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
}
