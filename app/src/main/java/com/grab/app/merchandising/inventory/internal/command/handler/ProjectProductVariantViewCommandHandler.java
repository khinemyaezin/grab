package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import com.inventory.application.model.write.ProjectProductVariantViewResult;
import com.inventory.application.port.inbound.ProjectProductVariantViewUseCase;
import org.springframework.stereotype.Component;

@Component
public class ProjectProductVariantViewCommandHandler implements CommandHandler<ProjectProductVariantViewCommand, ProjectProductVariantViewResult> {

    private final ProjectProductVariantViewUseCase projectProductVariantViewUseCase;

    public ProjectProductVariantViewCommandHandler(ProjectProductVariantViewUseCase projectProductVariantViewUseCase) {
        this.projectProductVariantViewUseCase = projectProductVariantViewUseCase;
    }

    @Override
    @InventoryTransactional
    public ProjectProductVariantViewResult handle(ProjectProductVariantViewCommand command) {
        return projectProductVariantViewUseCase.execute(command);
    }

    @Override
    public Class<ProjectProductVariantViewCommand> getCommandType() {
        return ProjectProductVariantViewCommand.class;
    }
}
