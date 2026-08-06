package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentCommand;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.entity.Price;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class CreateVariantPriceAssignmentCommandHandler
        implements CommandHandler<CreateVariantPriceAssignmentCommand, CreateVariantPriceAssignmentResult> {

    private final PriceSetRepository priceSetRepository;
    private final VariantPriceSetLinkRepository variantPriceSetLinkRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public CreateVariantPriceAssignmentResult handle(CreateVariantPriceAssignmentCommand command) {
        Instant now = Instant.now();
        PriceSet priceSet = PriceSet.create(idGenerator.generateId(), now);
        Price price = Price.createBase(
                idGenerator.generateId(),
                priceSet.getId(),
                command.title(),
                CurrencyCode.of(command.currencyCode()),
                MoneyAmount.of(command.amount()),
                command.minQuantity(),
                command.maxQuantity(),
                PricingResultMapper.toRules(idGenerator, command.rules())
        );
        priceSet.addPrice(price, now);
        PriceSet saved = priceSetRepository.save(priceSet);
        String priceSetId = saved.getId().getValue();
        variantPriceSetLinkRepository.save(new VariantPriceSetLinkView(
                command.variantId(),
                priceSetId,
                command.productId(),
                command.sku(),
                command.merchantId(),
                now,
                now
        ));
        return new CreateVariantPriceAssignmentResult(priceSetId);
    }

    @Override
    public Class<CreateVariantPriceAssignmentCommand> getCommandType() {
        return CreateVariantPriceAssignmentCommand.class;
    }
}
