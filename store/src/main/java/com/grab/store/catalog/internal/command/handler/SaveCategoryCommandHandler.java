package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.command.SaveCategoryCommand;
import com.grab.store.catalog.internal.command.SaveCategoryResult;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveCategoryCommandHandler implements CommandHandler<SaveCategoryCommand, SaveCategoryResult> {

    private final CategoryRepository categoryRepository;
    private final IdGenerator idGenerator;

    @Override
    @CatalogTransactional
    public SaveCategoryResult handle(SaveCategoryCommand command) {
        log.debug("Handling SaveCategoryCommand for category name: {}", command.name());

        Id categoryId = idGenerator.generateId();
        Category category = buildCategory(categoryId, command);

        categoryRepository.save(category);

        log.info("Category saved successfully: {}", category.getId().getValue());

        return new SaveCategoryResult(category.getId().getValue());
    }

    @Override
    public Class<SaveCategoryCommand> getCommandType() {
        return SaveCategoryCommand.class;
    }

    private Category buildCategory(Id categoryId, SaveCategoryCommand command) {
        if (isRoot(command.parentId())) {
            return Category.createRoot(categoryId, command.name());
        }

        Category parent = categoryRepository.find(command.parentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found: " + command.parentId().getValue()));

        return Category.createChild(categoryId, command.name(), parent);
    }

    private boolean isRoot(Id parentId) {
        return parentId == null || parentId.getValue() == null || parentId.getValue().isBlank();
    }
}
