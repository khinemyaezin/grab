package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.catalog.application.model.read.*;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryChildrenResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryLeavesResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryNodeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.CategoryChildrenDtoMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.CategoryDtoMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.CategoryLeavesDtoMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.CategoryNodeDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private static final Logger log = Loggers.getLogger(CategoryQueryService.class);

    private final QueryBus queryBus;
    private final CategoryDtoMapper categoryDtoMapper;
    private final CategoryLeavesDtoMapper categoryLeavesDtoMapper;
    private final CategoryNodeDtoMapper categoryNodeDtoMapper;
    private final CategoryChildrenDtoMapper categoryChildrenDtoMapper;

    public CategoryResponse getCategory(String categoryId) {
        log.info("Getting category: {}", categoryId);

        CategoryResult result = queryBus.dispatch(new GetCategoryQuery(categoryId));
        return categoryDtoMapper.toResponse(result);
    }

    public CategoryNodeResponse getCategoryTree(String categoryId) {
        log.info("Getting category tree: {}", categoryId);

        CategoryNodeResult result = queryBus.dispatch(new GetCategoryTreeQuery(categoryId));
        return categoryNodeDtoMapper.toResponse(result);
    }

    public CategoryResponse getCategoryParent(String categoryId) {
        log.info("Getting parent category for: {}", categoryId);

        CategoryResult result = queryBus.dispatch(new GetCategoryParentQuery(categoryId));
        return categoryDtoMapper.toResponse(result);
    }

    public CategoryLeavesResponse getLeafNodesByName(String name) {
        log.info("Getting category leaves by name: {}", name);

        CategoryLeavesResult result = queryBus.dispatch(new GetCategoryLeafNodesByNameQuery(name));
        return categoryLeavesDtoMapper.toResponse(result);
    }

    public CategoryChildrenResponse getCategoryChildren(String categoryId) {
        log.info("Getting category children for: {}", categoryId);

        CategoryChildrenResult result = queryBus.dispatch(new GetCategoryChildrenQuery(categoryId));
        return categoryChildrenDtoMapper.toResponse(result);
    }
}
