package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SaveCategoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeleteSellerCategoryMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.SaveSellerCategoryMapper;
import com.grab.app.platform.seller.internal.command.model.SellerCategoryCommands.DeleteSellerCategoryCommand;
import com.grab.app.platform.seller.internal.command.model.SellerCategoryCommands.SaveSellerCategoryCommand;
import com.grab.app.platform.seller.port.outbound.SellerCategoryOutboundPort.CategoryOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerCategoryCommandService {

    private final CommandBus commandBus;
    private final SaveSellerCategoryMapper saveCategoryMapper;
    private final DeleteSellerCategoryMapper deleteCategoryMapper;

    public SellerCategoryCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            SaveSellerCategoryMapper saveCategoryMapper,
            DeleteSellerCategoryMapper deleteCategoryMapper) {
        this.commandBus = commandBus;
        this.saveCategoryMapper = saveCategoryMapper;
        this.deleteCategoryMapper = deleteCategoryMapper;
    }

    public CategoryResponse saveCategory(SaveCategoryRequest request) {
        SaveSellerCategoryCommand command = saveCategoryMapper.toCommand(request);
        CategoryOutboundResult result = commandBus.dispatch(command);
        return saveCategoryMapper.toResponse(result);
    }

    public void deleteCategory(String categoryIdStr) {
        DeleteSellerCategoryCommand command = deleteCategoryMapper.toCommand(categoryIdStr);
        commandBus.dispatch(command);
    }
}
