package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import com.inventory.application.model.write.ProjectProductVariantViewResult;

public interface ProjectProductVariantViewUseCase {
    ProjectProductVariantViewResult execute(ProjectProductVariantViewCommand command);
}
