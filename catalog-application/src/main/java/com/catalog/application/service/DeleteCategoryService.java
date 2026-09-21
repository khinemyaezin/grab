package com.catalog.application.service;

import com.catalog.application.port.inbound.DeleteCategoryUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.Category;
import com.catalog.application.port.outbound.CategoryHierarchyPort;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.catalog.domain.port.outbound.ProductRepository;
import com.grab.framework.id.Id;
import com.catalog.application.model.write.DeleteCategoryCommand;
import com.catalog.application.model.write.DeleteCategoryResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

import java.util.Optional;
import java.util.Set;

@lombok.RequiredArgsConstructor
public class DeleteCategoryService implements DeleteCategoryUseCase {

    private static final Logger log = Loggers.getLogger(DeleteCategoryService.class);

    private final CategoryRepository categoryRepository;
    private final CategoryHierarchyPort categoryHierarchyPort;
    private final ProductRepository productRepository;

        public DeleteCategoryResult execute(DeleteCategoryCommand command) {
        log.debug("Handling DeleteCategoryCommand for category: {}", command.categoryId());

        Optional<Category> category = categoryRepository.find(command.categoryId());
        if (category.isEmpty()) {
            log.warn("Category not found for deletion: {}", command.categoryId());
            return new DeleteCategoryResult(false);
        }

        Set<Id> subtreeIds = categoryHierarchyPort.findSubtreeIds(command.categoryId());
        if (productRepository.existsByCategoryIds(subtreeIds)) {
            throw new CatalogServiceException(
                    new CatalogServiceError.CategoryHasAssignedProducts(command.categoryId().getValue())
            );
        }

        categoryHierarchyPort.deleteSubtree(command.categoryId());

        log.info("Category deleted successfully: {}", command.categoryId());

        return new DeleteCategoryResult(true);
    }
}
