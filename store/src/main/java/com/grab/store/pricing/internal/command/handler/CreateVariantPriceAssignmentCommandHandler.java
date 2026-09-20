package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.CreateVariantPriceAssignmentCommand;
import com.pricing.application.model.write.CreateVariantPriceAssignmentResult;
import com.pricing.application.port.inbound.CreateVariantPriceAssignmentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateVariantPriceAssignmentCommandHandler implements CommandHandler<CreateVariantPriceAssignmentCommand, CreateVariantPriceAssignmentResult> {

    private final CreateVariantPriceAssignmentUseCase createVariantPriceAssignmentUseCase;

    @Override
    @PricingTransactional
    public CreateVariantPriceAssignmentResult handle(CreateVariantPriceAssignmentCommand command) {
        return createVariantPriceAssignmentUseCase.execute(command);
    }

    @Override
    public Class<CreateVariantPriceAssignmentCommand> getCommandType() {
        return CreateVariantPriceAssignmentCommand.class;
    }
}
