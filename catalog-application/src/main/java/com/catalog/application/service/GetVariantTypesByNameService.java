package com.catalog.application.service;

import com.catalog.application.port.inbound.GetVariantTypesByNameUseCase;

import com.catalog.application.port.outbound.VariantTypeQueryPort;
import com.catalog.application.readmodel.VariantTypeView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.query.GetVariantTypesByNameQuery;
import com.catalog.application.query.VariantTypeResult;
import lombok.RequiredArgsConstructor;

import java.util.List;

@lombok.RequiredArgsConstructor
public class GetVariantTypesByNameService implements GetVariantTypesByNameUseCase {

    private static final Logger log = Loggers.getLogger(GetVariantTypesByNameService.class);

    private final VariantTypeQueryPort variantTypeQueryRepository;

    @Override
    public VariantTypeResult execute(GetVariantTypesByNameQuery query) {
        log.debug("Handling GetVariantTypesByNameQuery for name={}", query.name());

        List<VariantTypeView> views = variantTypeQueryRepository.findByName(query.name());

        List<VariantTypeResult.VariantTypeItem> items = views.stream()
                .map(this::toResultItem)
                .toList();

        return new VariantTypeResult(items);
    }

    private VariantTypeResult.VariantTypeItem toResultItem(VariantTypeView view) {

        return new VariantTypeResult.VariantTypeItem(
                view.id(),
                view.name()
        );
    }
}
