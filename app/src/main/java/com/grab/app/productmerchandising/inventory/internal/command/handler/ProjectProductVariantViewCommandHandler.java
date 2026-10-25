package com.grab.app.productmerchandising.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.productmerchandising.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import com.inventory.application.model.write.ProjectProductVariantViewResult;
import com.inventory.application.port.inbound.ProjectProductVariantViewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectProductVariantViewCommandHandler
        implements CommandHandler<ProjectProductVariantViewCommand, ProjectProductVariantViewResult> {
    private final ProjectProductVariantViewUseCase useCase;

    @Override
    @InventoryTransactional
    public ProjectProductVariantViewResult handle(ProjectProductVariantViewCommand command) {
        return useCase.execute(command);
    }

    @Override
    public Class<ProjectProductVariantViewCommand> getCommandType() {
        return ProjectProductVariantViewCommand.class;
    }
}
