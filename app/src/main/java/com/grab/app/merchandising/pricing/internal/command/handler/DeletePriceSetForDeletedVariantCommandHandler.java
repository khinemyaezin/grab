package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantCommand;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantResult;
import com.pricing.application.port.inbound.DeletePriceSetForDeletedVariantUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeletePriceSetForDeletedVariantCommandHandler
        implements CommandHandler<DeletePriceSetForDeletedVariantCommand, DeletePriceSetForDeletedVariantResult> {

    private final DeletePriceSetForDeletedVariantUseCase deletePriceSetForDeletedVariantUseCase;

    public DeletePriceSetForDeletedVariantCommandHandler(
            DeletePriceSetForDeletedVariantUseCase deletePriceSetForDeletedVariantUseCase) {
        this.deletePriceSetForDeletedVariantUseCase = deletePriceSetForDeletedVariantUseCase;
    }

    @Override
    @PricingTransactional
    public DeletePriceSetForDeletedVariantResult handle(DeletePriceSetForDeletedVariantCommand command) {
        return deletePriceSetForDeletedVariantUseCase.execute(command);
    }

    @Override
    public Class<DeletePriceSetForDeletedVariantCommand> getCommandType() {
        return DeletePriceSetForDeletedVariantCommand.class;
    }
}

