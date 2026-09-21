package com.catalog.application.service;

import com.catalog.application.port.inbound.GetVariantOptionsByNameUseCase;

import com.catalog.application.port.outbound.VariantOptionQueryPort;
import com.catalog.application.model.read.VariantOptionView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.model.read.GetVariantOptionsByNameQuery;
import com.catalog.application.model.read.VariantOptionResult;

import java.util.List;

@lombok.RequiredArgsConstructor
public class GetVariantOptionsByNameService implements GetVariantOptionsByNameUseCase {

    private static final Logger log = Loggers.getLogger(GetVariantOptionsByNameService.class);

    private final VariantOptionQueryPort variantOptionQueryRepository;

    @Override
    public VariantOptionResult execute(GetVariantOptionsByNameQuery query) {
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
}
