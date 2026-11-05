package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.CreateVariantPriceAssignmentCommand;
import com.pricing.application.model.write.CreateVariantPriceAssignmentResult;
import com.pricing.application.port.inbound.CreateVariantPriceAssignmentUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateVariantPriceAssignmentCommandHandler
        implements CommandHandler<CreateVariantPriceAssignmentCommand, CreateVariantPriceAssignmentResult> {

    private final CreateVariantPriceAssignmentUseCase createVariantPriceAssignmentUseCase;

    public CreateVariantPriceAssignmentCommandHandler(
            CreateVariantPriceAssignmentUseCase createVariantPriceAssignmentUseCase) {
        this.createVariantPriceAssignmentUseCase = createVariantPriceAssignmentUseCase;
    }

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

