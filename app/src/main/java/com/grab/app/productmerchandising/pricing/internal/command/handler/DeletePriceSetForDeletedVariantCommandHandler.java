package com.grab.app.productmerchandising.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.productmerchandising.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantCommand;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantResult;
import com.pricing.application.port.inbound.DeletePriceSetForDeletedVariantUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeletePriceSetForDeletedVariantCommandHandler implements CommandHandler<DeletePriceSetForDeletedVariantCommand, DeletePriceSetForDeletedVariantResult> {

    private final DeletePriceSetForDeletedVariantUseCase deletePriceSetForDeletedVariantUseCase;

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
