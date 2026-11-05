package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.UpdateVariantPriceCommand;
import com.pricing.application.model.write.UpdateVariantPriceResult;
import com.pricing.application.port.inbound.UpdateVariantPriceUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateVariantPriceCommandHandler
        implements CommandHandler<UpdateVariantPriceCommand, UpdateVariantPriceResult> {

    private final UpdateVariantPriceUseCase updateVariantPriceUseCase;

    public UpdateVariantPriceCommandHandler(UpdateVariantPriceUseCase updateVariantPriceUseCase) {
        this.updateVariantPriceUseCase = updateVariantPriceUseCase;
    }

    @Override
    @PricingTransactional
    public UpdateVariantPriceResult handle(UpdateVariantPriceCommand command) {
        return updateVariantPriceUseCase.execute(command);
    }

    @Override
    public Class<UpdateVariantPriceCommand> getCommandType() {
        return UpdateVariantPriceCommand.class;
    }
}

