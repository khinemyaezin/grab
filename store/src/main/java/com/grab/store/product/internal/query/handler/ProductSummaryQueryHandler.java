package com.grab.store.product.internal.query.handler;

import com.grab.store.product.internal.cqrs.query.QueryHandler;
import com.grab.store.product.internal.query.ProductSummaryQuery;
import com.grab.store.product.internal.query.ProductSummaryResult;
import com.grab.store.product.internal.util.PageInfoMapper;
import com.product.infrastructure.repository.jpa.ProductQueryRepository;
import com.product.infrastructure.specification.jpa.ProductSearchCriteria;
import com.product.infrastructure.specification.jpa.ProductSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSummaryQueryHandler implements QueryHandler<ProductSummaryQuery, ProductSummaryResult> {

    private final ProductQueryRepository productQueryRepository;

    @Override
    public ProductSummaryResult handle(ProductSummaryQuery query) {
        log.debug("Handling ProductSummaryQuery");

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .productName(query.productName())
                .sku(query.sku())
                .variantStatus(query.variantStatus())
                .variations(query.variations())
                .build();

        Page<ProductSummary> page = productQueryRepository.search(criteria, PageRequest.of(query.page(), query.size()));

        List<ProductSummaryResult.Product> products = page.getContent().stream()
                .map(summary -> new ProductSummaryResult.Product(
                        summary.id(),
                        summary.name(),
                        new ProductSummaryResult.VariantSummary(
                                summary.variants().available(),
                                summary.variants().options()
                        )
                ))
                .toList();

        return new ProductSummaryResult(
                products,
                PageInfoMapper.fromPage(page));
    }

    @Override
    public Class<ProductSummaryQuery> getQueryType() {
        return ProductSummaryQuery.class;
    }
}
