package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.VariantOptionView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.query.GetVariantOptionsByNameQuery;
import com.grab.store.catalog.internal.query.VariantOptionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetVariantOptionsByNameQueryHandler implements QueryHandler<GetVariantOptionsByNameQuery, VariantOptionResult> {

    private static final Logger log = Loggers.getLogger(GetVariantOptionsByNameQueryHandler.class);

    private final VariantOptionQueryRepository variantOptionQueryRepository;

    @Override
    public VariantOptionResult handle(GetVariantOptionsByNameQuery query) {
        log.debug("Handling GetVariantOptionsByNameQuery for name={}", query.name());

        List<VariantOptionView> views = variantOptionQueryRepository.findByNameAndTypeId(query.name(), query.typeId());

        List<VariantOptionResult.VariantOptionItem> items = views.stream()
                .map(view -> new VariantOptionResult.VariantOptionItem(
                        view.optionId(),
                        view.optionName(),
                        view.typeId(),
                        view.typeName()
                ))
                .toList();

        return new VariantOptionResult(items);
    }

    @Override
    public Class<GetVariantOptionsByNameQuery> getQueryType() {
        return GetVariantOptionsByNameQuery.class;
    }
}
