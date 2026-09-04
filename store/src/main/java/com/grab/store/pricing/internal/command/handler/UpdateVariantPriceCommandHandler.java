package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.UpdateVariantPriceCommand;
import com.grab.store.pricing.internal.command.UpdateVariantPriceResult;
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
import java.util.Optional;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class UpdateVariantPriceCommandHandler
        implements CommandHandler<UpdateVariantPriceCommand, UpdateVariantPriceResult> {

    private final PriceSetRepository priceSetRepository;
    private final VariantPriceSetLinkRepository variantPriceSetLinkRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public UpdateVariantPriceResult handle(UpdateVariantPriceCommand command) {
        Instant now = Instant.now();
        Optional<VariantPriceSetLinkView> existingLink =
                variantPriceSetLinkRepository.findByVariantId(command.variantId());
        if (existingLink.isEmpty()) {
            return createAssignment(command, now);
        }

        VariantPriceSetLinkView link = existingLink.get();
        Id priceSetId = idGenerator.convertIdFrom(link.priceSetId());
        Optional<PriceSet> existingPriceSet = priceSetRepository.findById(priceSetId);
        return existingPriceSet.map(priceSet -> updateAssignment(command, priceSet, link, now))
                .orElseGet(() -> createAssignment(command, now));

    }

    @Override
    public Class<UpdateVariantPriceCommand> getCommandType() {
        return UpdateVariantPriceCommand.class;
    }

    private UpdateVariantPriceResult createAssignment(UpdateVariantPriceCommand command, Instant now) {
        PriceSet priceSet = PriceSet.create(idGenerator.generateId(), now);
        Price price = toBasePrice(command, priceSet.getId());
        priceSet.addPrice(price, now);
        PriceSet saved = priceSetRepository.save(priceSet);
        String priceSetId = saved.getId().getValue();
        saveLink(command, priceSetId, now, now);
        return new UpdateVariantPriceResult(priceSetId, price.getId().getValue(), true);
    }

    private UpdateVariantPriceResult updateAssignment(
            UpdateVariantPriceCommand command,
            PriceSet priceSet,
            VariantPriceSetLinkView existingLink,
            Instant now
    ) {
        Price candidate = toBasePrice(command, priceSet.getId());
        Price applied = priceSet.applyBasePrice(candidate, now);
        PriceSet saved = priceSetRepository.save(priceSet);
        String priceSetId = saved.getId().getValue();
        Instant createdAt = existingLink.createdAt() == null ? now : existingLink.createdAt();
        saveLink(command, priceSetId, createdAt, now);
        return new UpdateVariantPriceResult(priceSetId, applied.getId().getValue(), false);
    }

    private Price toBasePrice(UpdateVariantPriceCommand command, Id priceSetId) {
        return Price.createBase(
                idGenerator.generateId(),
                priceSetId,
                command.title(),
                CurrencyCode.of(command.currencyCode()),
                MoneyAmount.of(command.amount()),
                command.minQuantity(),
                command.maxQuantity(),
                PricingResultMapper.toRules(idGenerator, command.rules())
        );
    }

    private void saveLink(
            UpdateVariantPriceCommand command,
            String priceSetId,
            Instant createdAt,
            Instant updatedAt
    ) {
        variantPriceSetLinkRepository.save(new VariantPriceSetLinkView(
                command.variantId(),
                priceSetId,
                command.productId(),
                command.sku(),
                command.merchantId(),
                createdAt,
                updatedAt
        ));
    }
}
