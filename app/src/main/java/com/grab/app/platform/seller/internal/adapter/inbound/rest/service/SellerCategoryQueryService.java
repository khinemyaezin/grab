package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryNodeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerCategoryChildrenMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerCategoryLeavesMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerCategoryMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerCategoryTreeMapper;
import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryChildrenQuery;
import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryLeavesQuery;
import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryQuery;
import com.grab.app.platform.seller.internal.query.model.SellerCategoryQueries.GetSellerCategoryTreeQuery;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryNodeOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerCategoryQueryService {

    private final QueryBus queryBus;
    private final GetSellerCategoryMapper getCategoryMapper;
    private final GetSellerCategoryTreeMapper getCategoryTreeMapper;
    private final GetSellerCategoryChildrenMapper getChildrenMapper;
    private final GetSellerCategoryLeavesMapper getLeavesMapper;

    public SellerCategoryQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerCategoryMapper getCategoryMapper,
            GetSellerCategoryTreeMapper getCategoryTreeMapper,
            GetSellerCategoryChildrenMapper getChildrenMapper,
            GetSellerCategoryLeavesMapper getLeavesMapper) {
        this.queryBus = queryBus;
        this.getCategoryMapper = getCategoryMapper;
        this.getCategoryTreeMapper = getCategoryTreeMapper;
        this.getChildrenMapper = getChildrenMapper;
        this.getLeavesMapper = getLeavesMapper;
    }

    public Optional<CategoryResponse> getCategory(String categoryIdStr) {
        GetSellerCategoryQuery query = getCategoryMapper.toQuery(categoryIdStr);
        Optional<CategoryOutboundResult> result = queryBus.dispatch(query);
        return result.map(getCategoryMapper::toResponse);
    }

    public CategoryNodeResponse getCategoryTree(String rootIdStr) {
        GetSellerCategoryTreeQuery query = getCategoryTreeMapper.toQuery(rootIdStr);
        CategoryNodeOutboundResult result = queryBus.dispatch(query);
        return getCategoryTreeMapper.toResponse(result);
    }

    public List<CategoryResponse> getChildren(String categoryIdStr) {
        GetSellerCategoryChildrenQuery query = getChildrenMapper.toQuery(categoryIdStr);
        List<CategoryOutboundResult> results = queryBus.dispatch(query);
        return results != null ? getChildrenMapper.toResponseList(results) : List.of();
    }

    public List<CategoryResponse> getLeafCategories(String name) {
        GetSellerCategoryLeavesQuery query = getLeavesMapper.toQuery(name);
        List<CategoryOutboundResult> results = queryBus.dispatch(query);
        return results != null ? getLeavesMapper.toResponseList(results) : List.of();
    }
}
