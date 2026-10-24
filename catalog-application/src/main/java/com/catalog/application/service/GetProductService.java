package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.ProductPublicationView;
import com.catalog.application.port.inbound.GetProductUseCase;
import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetProductService implements GetProductUseCase {

    private static final Logger log = Loggers.getLogger(GetProductService.class);

    private final ProductQueryPort productQueryPort;
    private final ProductDetailResultAssembler assembler;

    public GetProductService(
            ProductQueryPort productQueryPort,
            VariantOptionQueryPort variantOptionQueryPort,
            IdGenerator idGenerator,
            CategoryQueryPort categoryQueryPort,
            MatrixKeyGenerator matrixKeyGenerator,
            ProductMediaConverter productMediaConverter
    ) {
        this(
                productQueryPort,
                new ProductDetailResultAssembler(
                        variantOptionQueryPort,
                        idGenerator,
                        categoryQueryPort,
                        matrixKeyGenerator,
                        productMediaConverter
                )
        );
    }

    @Override
    public GetProductResult execute(GetProductQuery query) {
        log.debug("Handling GetProductQuery for productId: {}", query.productId());

        ProductDetailView product = productQueryPort.findDetailByIdAndMerchantId(query.productId(), query.merchantId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ProductNotFound(query.productId())
                ));

        List<ProductPublicationView> publicationViews = productQueryPort
                .findPublicationsByProductIds(List.of(query.productId()));
        return assembler.assemble(product, publicationViews);
    }
}
