package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.ReplacePriceListRulesCommand;
import com.pricing.application.port.inbound.ReplacePriceListRulesUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReplacePriceListRulesCommandHandler
        implements CommandHandler<ReplacePriceListRulesCommand, PriceListResult> {

    private final ReplacePriceListRulesUseCase replacePriceListRulesUseCase;

    public ReplacePriceListRulesCommandHandler(ReplacePriceListRulesUseCase replacePriceListRulesUseCase) {
        this.replacePriceListRulesUseCase = replacePriceListRulesUseCase;
    }

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

