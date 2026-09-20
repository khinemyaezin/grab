package com.catalog.application.service;

import com.catalog.application.port.inbound.SaveCategoryUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.catalog.application.command.SaveCategoryCommand;
import com.catalog.application.command.SaveCategoryResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class SaveCategoryService implements SaveCategoryUseCase {

    private static final Logger log = Loggers.getLogger(SaveCategoryService.class);

    private final CategoryRepository categoryRepository;
    private final IdGenerator idGenerator;

        public SaveCategoryResult execute(SaveCategoryCommand command) {
        log.debug("Handling SaveCategoryCommand for category name: {}", command.name());

        Id categoryId = idGenerator.generateId();
        Category category = buildCategory(categoryId, command);

        categoryRepository.save(category);

        log.info("Category saved successfully: {}", category.getId().getValue());

        return new SaveCategoryResult(category.getId().getValue());
    }

    private Category buildCategory(Id categoryId, SaveCategoryCommand command) {
        if (isRoot(command.parentId())) {
            return Category.createRoot(categoryId, command.name(), true, false, false);
        }

        Category parent = categoryRepository.find(command.parentId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ParentCategoryNotFound(command.parentId().getValue())
                ));

        return Category.createChild(
                categoryId,
                command.name(),
                parent,
                command.active(),
                command.listingAllowed(),
                command.c2cAllowed());
    }

    private boolean isRoot(Id parentId) {
        return parentId == null || parentId.getValue() == null || parentId.getValue().isBlank();
    }
}
