package com.grab.store.catalog.internal.command.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.catalog.domain.repository.ProductRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.command.DeleteCategoryCommand;
import com.grab.store.catalog.internal.command.DeleteCategoryResult;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DeleteCategoryCommandHandler implements CommandHandler<DeleteCategoryCommand, DeleteCategoryResult> {

    private static final Logger log = Loggers.getLogger(DeleteCategoryCommandHandler.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @CatalogTransactional
    public DeleteCategoryResult handle(DeleteCategoryCommand command) {
        log.debug("Handling DeleteCategoryCommand for category: {}", command.categoryId());

        Optional<Category> category = categoryRepository.find(command.categoryId());
        if (category.isEmpty()) {
            log.warn("Category not found for deletion: {}", command.categoryId());
            return new DeleteCategoryResult(false);
        }

        Set<Id> subtreeIds = categoryRepository.findSubtreeIds(command.categoryId());
        if (productRepository.existsByCategoryIds(subtreeIds)) {
            throw new CatalogServiceException(
                    new CatalogServiceError.CategoryHasAssignedProducts(command.categoryId().getValue())
            );
        }

        categoryRepository.deleteCascade(category.get());

        log.info("Category deleted successfully: {}", command.categoryId());

        return new DeleteCategoryResult(true);
    }

    @Override
    public Class<DeleteCategoryCommand> getCommandType() {
        return DeleteCategoryCommand.class;
    }
}
