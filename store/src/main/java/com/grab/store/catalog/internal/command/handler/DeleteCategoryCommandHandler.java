package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.grab.store.catalog.internal.command.DeleteCategoryCommand;
import com.grab.store.catalog.internal.command.DeleteCategoryResult;
import com.grab.store.catalog.internal.cqrs.command.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteCategoryCommandHandler implements CommandHandler<DeleteCategoryCommand, DeleteCategoryResult> {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public DeleteCategoryResult handle(DeleteCategoryCommand command) {
        log.debug("Handling DeleteCategoryCommand for category: {}", command.categoryId());

        Optional<Category> category = categoryRepository.find(command.categoryId());
        if (category.isEmpty()) {
            log.warn("Category not found for deletion: {}", command.categoryId());
            return new DeleteCategoryResult(false);
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
