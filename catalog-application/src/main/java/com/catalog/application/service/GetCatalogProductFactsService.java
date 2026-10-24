package com.catalog.application.service;

import com.catalog.application.model.read.GetCatalogProductFactsQuery;
import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.ProductDetailView;
import com.catalog.application.model.read.ProductPublicationView;
import com.catalog.application.port.inbound.GetCatalogProductFactsUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import com.catalog.application.port.outbound.ProductQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GetCatalogProductFactsService implements GetCatalogProductFactsUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;
    private final ProductQueryPort productQueryPort;
    private final ProductDetailResultAssembler assembler;

    @Override
    public Optional<GetCatalogProductFactsResult> execute(GetCatalogProductFactsQuery query) {
        String productId = query.productId();
        List<String> variantIds = buyabilityQueryPort.variantIdsForProduct(productId);
        List<String> orderedVariantIds = variantIds.stream().sorted().toList();
        for (String variantId : orderedVariantIds) {
            Optional<BuyabilityQueryPort.VariantSlice> variant = buyabilityQueryPort.findVariant(variantId);
            if (variant.isEmpty()) {
                continue;
            }
            String merchantId = variant.get().sellerId();
            Optional<ProductDetailView> productOpt = productQueryPort.findDetailByIdAndMerchantId(productId, merchantId);
            if (productOpt.isEmpty()) {
                continue;
            }
            List<ProductPublicationView> publicationViews = productQueryPort
                    .findPublicationsByProductIds(List.of(productId));
            GetProductResult product = assembler.assemble(productOpt.get(), publicationViews);
            return Optional.of(new GetCatalogProductFactsResult(productId, merchantId, product));
        }
        return Optional.empty();
    }
}
