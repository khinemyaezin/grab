package com.grab.app.merchandising.catalog.internal.adapter.inbound.message;

import com.catalog.application.model.read.CategoryChildrenResult;
import com.catalog.application.model.read.CategoryLeavesResult;
import com.catalog.application.model.read.CategoryNodeResult;
import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryChildrenQuery;
import com.catalog.application.model.read.GetCategoryLeafNodesByNameQuery;
import com.catalog.application.model.read.GetCategoryQuery;
import com.catalog.application.model.read.GetCategoryTreeQuery;
import com.catalog.application.model.write.DeleteCategoryCommand;
import com.catalog.application.model.write.SaveCategoryCommand;
import com.catalog.application.model.write.SaveCategoryResult;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.DeleteCategoryPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetCategoryChildrenPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetCategoryLeavesPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetCategoryPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetCategoryTreePortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.SaveCategoryPortMapper;
import com.grab.app.merchandising.catalog.port.message.CatalogCategoryPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class CatalogCategoryPortAdapter implements CatalogCategoryPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final SaveCategoryPortMapper saveCategoryMapper;
    private final DeleteCategoryPortMapper deleteCategoryMapper;
    private final GetCategoryPortMapper getCategoryMapper;
    private final GetCategoryTreePortMapper getCategoryTreeMapper;
    private final GetCategoryChildrenPortMapper getCategoryChildrenMapper;
    private final GetCategoryLeavesPortMapper getCategoryLeavesMapper;

    public CatalogCategoryPortAdapter(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            SaveCategoryPortMapper saveCategoryMapper,
            DeleteCategoryPortMapper deleteCategoryMapper,
            GetCategoryPortMapper getCategoryMapper,
            GetCategoryTreePortMapper getCategoryTreeMapper,
            GetCategoryChildrenPortMapper getCategoryChildrenMapper,
            GetCategoryLeavesPortMapper getCategoryLeavesMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.saveCategoryMapper = saveCategoryMapper;
        this.deleteCategoryMapper = deleteCategoryMapper;
        this.getCategoryMapper = getCategoryMapper;
        this.getCategoryTreeMapper = getCategoryTreeMapper;
        this.getCategoryChildrenMapper = getCategoryChildrenMapper;
        this.getCategoryLeavesMapper = getCategoryLeavesMapper;
    }

    @Override
    public CategoryMessageResult createCategory(CreateCategoryMessage message) {
        SaveCategoryCommand command = saveCategoryMapper.toCommand(message);
        SaveCategoryResult result = commandBus.dispatch(command);
        return saveCategoryMapper.toResponse(result, message);
    }

    @Override
    public void deleteCategory(Id categoryId) {
        DeleteCategoryCommand command = deleteCategoryMapper.toCommand(categoryId);
        commandBus.dispatch(command);
    }

    @Override
    public Optional<CategoryMessageResult> findCategoryById(Id categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }
        GetCategoryQuery query = getCategoryMapper.toQuery(categoryId);
        CategoryResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getCategoryMapper.toResponse(result));
    }

    @Override
    public CategoryTreeNodeResult getCategoryTree(Id rootCategoryId) {
        GetCategoryTreeQuery query = getCategoryTreeMapper.toQuery(rootCategoryId);
        CategoryNodeResult result = queryBus.dispatch(query);
        return getCategoryTreeMapper.toResponse(result);
    }

    @Override
    public List<CategoryMessageResult> getChildCategories(Id categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        GetCategoryChildrenQuery query = getCategoryChildrenMapper.toQuery(categoryId);
        CategoryChildrenResult result = queryBus.dispatch(query);
        return getCategoryChildrenMapper.toResponse(result);
    }

    @Override
    public List<CategoryMessageResult> getLeafCategories(String name) {
        GetCategoryLeafNodesByNameQuery query = getCategoryLeavesMapper.toQuery(name);
        CategoryLeavesResult result = queryBus.dispatch(query);
        return getCategoryLeavesMapper.toResponse(result);
    }
}
