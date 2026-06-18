package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveCategoryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteCategoryResponse;
import com.grab.store.catalog.internal.api.rest.mapper.SaveCategoryDtoMapper;
import com.grab.store.catalog.internal.command.DeleteCategoryCommand;
import com.grab.store.catalog.internal.command.DeleteCategoryResult;
import com.grab.store.catalog.internal.command.SaveCategoryCommand;
import com.grab.store.catalog.internal.command.SaveCategoryResult;
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
