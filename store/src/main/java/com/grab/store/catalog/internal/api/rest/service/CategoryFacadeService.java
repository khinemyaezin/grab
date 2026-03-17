package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.assembler.CategoryChildrenModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.CategoryModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.CategoryNodeModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.DeleteCategoryModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveCategoryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryChildrenResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryNodeResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteCategoryResponse;
import com.grab.store.catalog.internal.api.rest.mapper.CategoryChildrenDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.CategoryDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.CategoryNodeDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.SaveCategoryDtoMapper;
import com.grab.store.catalog.internal.command.DeleteCategoryCommand;
import com.grab.store.catalog.internal.command.DeleteCategoryResult;
import com.grab.store.catalog.internal.command.SaveCategoryCommand;
import com.grab.store.catalog.internal.command.SaveCategoryResult;
import com.grab.store.catalog.internal.query.*;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryFacadeService {

    private static final Logger log = Loggers.getLogger(CategoryFacadeService.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final SaveCategoryDtoMapper saveCategoryDtoMapper;
    private final CategoryDtoMapper categoryDtoMapper;
    private final CategoryNodeDtoMapper categoryNodeDtoMapper;
    private final CategoryChildrenDtoMapper categoryChildrenDtoMapper;
    private final CategoryModelAssembler categoryModelAssembler;
    private final CategoryNodeModelAssembler categoryNodeModelAssembler;
    private final CategoryChildrenModelAssembler categoryChildrenModelAssembler;
    private final DeleteCategoryModelAssembler deleteCategoryModelAssembler;
    private final IdGenerator idGenerator;

    public String saveCategory(SaveCategoryRequest request) {
        log.info("Saving category: {}", request.name());

        SaveCategoryCommand command = saveCategoryDtoMapper.toCommand(request);
        SaveCategoryResult result = commandBus.dispatch(command);

        return result.categoryId();
    }

    public EntityModel<CategoryResponse> getCategory(String categoryId) {
        log.info("Getting category: {}", categoryId);

        CategoryResult result = queryBus.dispatch(new GetCategoryQuery(categoryId));
        CategoryResponse response = categoryDtoMapper.toResponse(result);
        return categoryModelAssembler.toModel(response);
    }

    public EntityModel<CategoryNodeResponse> getCategoryTree() {
        log.info("Getting category tree");

        CategoryNodeResult result = queryBus.dispatch(new GetCategoryTreeQuery());
        CategoryNodeResponse response = categoryNodeDtoMapper.toResponse(result);
        return categoryNodeModelAssembler.toModel(response);
    }

    public EntityModel<CategoryResponse> getCategoryParent(String categoryId) {
        log.info("Getting parent category for: {}", categoryId);

        CategoryResult result = queryBus.dispatch(new GetCategoryParentQuery(categoryId));
        CategoryResponse response = categoryDtoMapper.toResponse(result);
        return categoryModelAssembler.toModel(response);
    }

    public EntityModel<CategoryChildrenResponse> getCategoryChildren(String categoryId) {
        log.info("Getting category children for: {}", categoryId);

        CategoryChildrenResult result = queryBus.dispatch(new GetCategoryChildrenQuery(categoryId));
        CategoryChildrenResponse response = categoryChildrenDtoMapper.toResponse(result);
        return categoryChildrenModelAssembler.toModel(response);
    }

    public EntityModel<DeleteCategoryResponse> deleteCategory(String categoryId) {
        log.info("Deleting category: {}", categoryId);

        DeleteCategoryCommand command = new DeleteCategoryCommand(idGenerator.generateId(categoryId));
        DeleteCategoryResult result = commandBus.dispatch(command);
        DeleteCategoryResponse response = new DeleteCategoryResponse(categoryId, result.deleted());

        return deleteCategoryModelAssembler.toModel(response);
    }
}
