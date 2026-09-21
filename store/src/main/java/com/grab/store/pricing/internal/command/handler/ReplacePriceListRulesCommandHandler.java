package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.ReplacePriceListRulesCommand;
import com.pricing.application.port.inbound.ReplacePriceListRulesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplacePriceListRulesCommandHandler implements CommandHandler<ReplacePriceListRulesCommand, PriceListResult> {

    private final ReplacePriceListRulesUseCase replacePriceListRulesUseCase;

    @Override
    @PricingTransactional
    public PriceListResult handle(ReplacePriceListRulesCommand command) {
        return replacePriceListRulesUseCase.execute(command);
    }

    @Override
    public Class<ReplacePriceListRulesCommand> getCommandType() {
        return ReplacePriceListRulesCommand.class;
    }
}
