package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.ProductSummary;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.ProductSummaryQuery;
import com.grab.store.catalog.internal.query.ProductSummaryResult;
import com.grab.store.catalog.internal.query.SpringPageInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductSummaryQueryHandler implements QueryHandler<ProductSummaryQuery, ProductSummaryResult> {

    private static final Logger log = Loggers.getLogger(ProductSummaryQueryHandler.class);

    private final ProductQueryRepository productQueryRepository;

    @Override
    @CatalogReadTransactional
    public ProductSummaryResult handle(ProductSummaryQuery query) {
        log.debug("Handling ProductSummaryQuery");

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .productName(query.productName())
                .sku(query.sku())
                .variantStatus(query.variantStatus())
                .categoryId(query.categoryId())
                .sellerId(query.sellerId())
                .sellerType(query.sellerType())
                .offerEligible(query.offerEligible())
                .build();

        Page<ProductSummary> page = productQueryRepository.search(criteria, PageRequest.of(query.page(), query.size()));
        List<ProductSummaryResult.Product> products = mapToResultProducts(page.getContent());

        return new ProductSummaryResult(
                products,
                SpringPageInfoFactory.toPageInfo(page));
    }

    private List<ProductSummaryResult.Product> mapToResultProducts(List<ProductSummary> summaries) {
        return summaries.stream()
                .map(summary -> new ProductSummaryResult.Product(
                        summary.id(),
                        summary.name(),
                        summary.sellerId(),
                        summary.sellerType(),
                        summary.offerEligible(),
                        summary.status(),
                        summary.slug(),
                        summary.featured(),
                        new ProductSummaryResult.VariantSummary(
                                summary.variantSummary().available(),
                                extractVariantTypes(summary.variantSummary())
                        )
                ))
                .toList();
    }

    private List<ProductSummaryResult.VariantType> extractVariantTypes(ProductSummary.VariantSummary variantSummary) {
        return variantSummary.types().stream()
                .map(type -> new ProductSummaryResult.VariantType(
                        type.typeId(),
                        "",
                        type.options().stream().map(
                                option -> new ProductSummaryResult.VariantOption(
                                        option.optionId(),
                                        ""
                        )).toList()
                ))
                .toList();
    }

    @Override
    public Class<ProductSummaryQuery> getQueryType() {
        return ProductSummaryQuery.class;
    }
}
