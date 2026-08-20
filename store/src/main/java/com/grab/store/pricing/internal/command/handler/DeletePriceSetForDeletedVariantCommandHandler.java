package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.pricing.internal.command.DeletePriceSetForDeletedVariantCommand;
import com.grab.store.pricing.internal.command.DeletePriceSetForDeletedVariantResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkQueryRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class DeletePriceSetForDeletedVariantCommandHandler
        implements CommandHandler<DeletePriceSetForDeletedVariantCommand, DeletePriceSetForDeletedVariantResult> {

    private static final Logger log = Loggers.getLogger(DeletePriceSetForDeletedVariantCommandHandler.class);

    private final VariantPriceSetLinkQueryRepository variantPriceSetLinkQueryRepository;
    private final VariantPriceSetLinkRepository variantPriceSetLinkRepository;
    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public DeletePriceSetForDeletedVariantResult handle(DeletePriceSetForDeletedVariantCommand command) {
        String variantId = command.variantId().getValue();
        List<VariantPriceSetLinkView> links = variantPriceSetLinkQueryRepository.findByVariantIds(List.of(variantId));
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

    @Override
    public Class<DeletePriceSetForDeletedVariantCommand> getCommandType() {
        return DeletePriceSetForDeletedVariantCommand.class;
    }
}
