package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.SaveCategoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.DeleteCategoryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.SaveCategoryDtoMapper;
import com.catalog.application.model.write.DeleteCategoryCommand;
import com.catalog.application.model.write.DeleteCategoryResult;
import com.catalog.application.model.write.SaveCategoryCommand;
import com.catalog.application.model.write.SaveCategoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryCommandService {

    private static final Logger log = Loggers.getLogger(CategoryCommandService.class);

    private final CommandBus commandBus;
    private final SaveCategoryDtoMapper saveCategoryDtoMapper;
    private final IdGenerator idGenerator;

    public String saveCategory(SaveCategoryRequest request) {
        log.info("Saving category: {}", request.name());

        SaveCategoryCommand command = saveCategoryDtoMapper.toCommand(request);
        SaveCategoryResult result = commandBus.dispatch(command);

        return result.categoryId();
    }

    public DeleteCategoryResponse deleteCategory(String categoryId) {
        log.info("Deleting category: {}", categoryId);

        DeleteCategoryCommand command = new DeleteCategoryCommand(idGenerator.convertIdFrom(categoryId));
        DeleteCategoryResult result = commandBus.dispatch(command);

        return new DeleteCategoryResponse(categoryId, result.deleted());
    }
}
