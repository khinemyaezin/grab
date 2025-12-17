package com.grab.store.product.internal.query.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.cqrs.query.QueryHandler;
import com.grab.store.product.internal.query.ProductCombinationQuery;
import com.grab.store.product.internal.query.ProductCombinationResult;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.aggregate.product.VariantType;
import com.product.domain.service.VariantCombination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCombinationQueryHandler
        implements QueryHandler<ProductCombinationQuery, ProductCombinationResult> {

    private final VariantCombination variantCombinationService;
    private final IdGenerator idGenerator;

    @Override
    public ProductCombinationResult handle(ProductCombinationQuery query) {
        log.debug("Generating combinations for {} variant types",
                query.desiredVariantTypes().size());

        List<VariantType> domainVariantTypes = mapToDomainVariantTypes(query);

        List<List<VariantOption>> combinations =
                variantCombinationService.generateCombinations(domainVariantTypes);

        List<ProductCombinationResult.VariantCombination> resultCombinations =
                combinations.stream()
                        .map(this::mapToResultCombination)
                        .toList();

        List<ProductCombinationResult.VariantType> requestedTypes =
                query.desiredVariantTypes().stream()
                        .map(vt -> new ProductCombinationResult.VariantType(
                                vt.typeId(),
                                vt.typeName(),
                                vt.options().stream()
                                        .map( opt -> new ProductCombinationResult.VariantOption(
                                                opt.optionId(),
                                                opt.optionName()
                                        ))
                                        .toList()
                        ))
                        .toList();

        log.info("Generated {} combinations", resultCombinations.size());

        return new ProductCombinationResult(
                requestedTypes,
                resultCombinations,
                resultCombinations.size()
        );
    }

    private List<VariantType> mapToDomainVariantTypes(ProductCombinationQuery query) {
        return query.desiredVariantTypes().stream()
                .map(queryType -> {
                    VariantType domainType = new VariantType(queryType.typeId(), queryType.typeName());
                    queryType.options().forEach(opt ->
                            domainType.addOption(new VariantOption(opt.optionId(), opt.optionName(), domainType))
                    );
                    return domainType;
                })
                .toList();
    }

    private ProductCombinationResult.VariantCombination mapToResultCombination(
            List<VariantOption> options) {

        Id combinationId = idGenerator.generateId();

        List<ProductCombinationResult.Variation> variations = options.stream()
                .map(opt -> new ProductCombinationResult.Variation(
                        opt.getId(),
                        opt.getName(),
                        opt.getVariantType().getId(),
                        opt.getVariantType().getName()
                ))
                .toList();

        return new ProductCombinationResult.VariantCombination(
                combinationId,
                variations
        );
    }

    @Override
    public Class<ProductCombinationQuery> getQueryType() {
        return ProductCombinationQuery.class;
    }
}
