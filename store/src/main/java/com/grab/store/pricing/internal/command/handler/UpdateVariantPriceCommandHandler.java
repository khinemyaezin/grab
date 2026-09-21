package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.UpdateVariantPriceCommand;
import com.pricing.application.model.write.UpdateVariantPriceResult;
import com.pricing.application.port.inbound.UpdateVariantPriceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateVariantPriceCommandHandler implements CommandHandler<UpdateVariantPriceCommand, UpdateVariantPriceResult> {

    private final UpdateVariantPriceUseCase updateVariantPriceUseCase;

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
