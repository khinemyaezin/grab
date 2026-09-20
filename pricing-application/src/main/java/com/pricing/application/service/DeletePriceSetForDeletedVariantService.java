package com.pricing.application.service;

import com.pricing.application.port.inbound.DeletePriceSetForDeletedVariantUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantCommand;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantResult;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DeletePriceSetForDeletedVariantService implements DeletePriceSetForDeletedVariantUseCase {

    private static final Logger log = Loggers.getLogger(DeletePriceSetForDeletedVariantService.class);

    private final VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort;
    private final VariantPriceSetLinkRepository variantPriceSetLinkRepository;
    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;
    public DeletePriceSetForDeletedVariantResult execute(DeletePriceSetForDeletedVariantCommand command) {
        String variantId = command.variantId().getValue();
        List<VariantPriceSetLinkView> links = variantPriceSetLinkQueryPort.findByVariantIds(List.of(variantId));
        if (links.isEmpty()) {
            log.info("No price set link for deleted variantId={}", variantId);
            return new DeletePriceSetForDeletedVariantResult(variantId, null, false);
        }

        VariantPriceSetLinkView link = links.getFirst();
        String priceSetIdValue = link.priceSetId();
        variantPriceSetLinkRepository.deleteByVariantId(variantId);
        Id priceSetId = idGenerator.convertIdFrom(priceSetIdValue);
        if (priceSetRepository.findById(priceSetId).isPresent()) {
            priceSetRepository.delete(priceSetId);
        }
        log.info("Deleted price set for deleted variantId={} priceSetId={}", variantId, priceSetIdValue);
        return new DeletePriceSetForDeletedVariantResult(variantId, priceSetIdValue, true);
    }
}
