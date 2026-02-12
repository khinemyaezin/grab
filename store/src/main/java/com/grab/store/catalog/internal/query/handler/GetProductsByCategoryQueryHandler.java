package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.grab.store.catalog.internal.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.query.GetProductsByCategoryQuery;
import com.grab.store.catalog.internal.query.ProductSummaryResult;
import com.grab.store.catalog.internal.util.PageInfoMapper;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.view.ProductSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductsByCategoryQueryHandler implements QueryHandler<GetProductsByCategoryQuery, ProductSummaryResult> {

    private final ProductQueryRepository productQueryRepository;

    @Override
    public ProductSummaryResult handle(GetProductsByCategoryQuery query) {
        log.debug("Handling GetProductsByCategoryQuery for categoryId={}", query.categoryId());

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .categoryId(query.categoryId())
                .productStatus(ProductStatus.ACTIVE.name())
                .build();

        Page<ProductSummary> page = productQueryRepository.search(
                criteria,
                PageRequest.of(query.page(), query.size())
        );

        List<ProductSummaryResult.Product> products = mapToResultProducts(page.getContent());

        return new ProductSummaryResult(products, PageInfoMapper.fromPage(page));
    }

    private List<ProductSummaryResult.Product> mapToResultProducts(List<ProductSummary> summaries) {
        return summaries.stream()
                .map(summary -> new ProductSummaryResult.Product(
                        summary.id(),
                        summary.name(),
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
    public Class<GetProductsByCategoryQuery> getQueryType() {
        return GetProductsByCategoryQuery.class;
    }
}
