package com.catalog.application.service;

import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.model.read.GetCatalogVariantFactsQuery;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.ProductPublicationView;
import com.catalog.application.port.inbound.GetCatalogVariantFactsUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GetCatalogVariantFactsService implements GetCatalogVariantFactsUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;
    private final ProductQueryPort productQueryPort;
    private final ProductDetailResultAssembler assembler;

    @Override
    public Optional<GetCatalogProductFactsResult> execute(GetCatalogVariantFactsQuery query) {
        Optional<BuyabilityQueryPort.VariantSlice> variant = buyabilityQueryPort.findVariant(query.variantId());
        if (variant.isEmpty()) {
            return Optional.empty();
        }
        String productId = variant.get().productId();
        String merchantId = variant.get().sellerId();
        Optional<ProductDetailView> productOpt = productQueryPort.findDetailByIdAndMerchantId(productId, merchantId);
        if (productOpt.isEmpty()) {
            return Optional.empty();
        }
        List<ProductPublicationView> publicationViews = productQueryPort
                .findPublicationsByProductIds(List.of(productId));
        GetProductResult product = assembler.assemble(productOpt.get(), publicationViews);
        return Optional.of(new GetCatalogProductFactsResult(productId, merchantId, product));
    }
}
