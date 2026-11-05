package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductStatusRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerProductMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeleteSellerProductMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.UpdateSellerProductMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.UpdateSellerProductStatusMapper;
import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.CreateSellerProductCommand;
import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.DeleteSellerProductCommand;
import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.UpdateSellerProductCommand;
import com.grab.app.platform.seller.internal.command.model.SellerProductCommands.UpdateSellerProductStatusCommand;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerProductCommandService {

    private final CommandBus commandBus;
    private final CreateSellerProductMapper createProductMapper;
    private final UpdateSellerProductMapper updateProductMapper;
    private final DeleteSellerProductMapper deleteProductMapper;
    private final UpdateSellerProductStatusMapper updateProductStatusMapper;

    public SellerProductCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            CreateSellerProductMapper createProductMapper,
            UpdateSellerProductMapper updateProductMapper,
            DeleteSellerProductMapper deleteProductMapper,
            UpdateSellerProductStatusMapper updateProductStatusMapper) {
        this.commandBus = commandBus;
        this.createProductMapper = createProductMapper;
        this.updateProductMapper = updateProductMapper;
        this.deleteProductMapper = deleteProductMapper;
        this.updateProductStatusMapper = updateProductStatusMapper;
    }

    public ProductResponse createProduct(String merchantIdStr, CreateProductRequest request) {
        CreateSellerProductCommand command = createProductMapper.toCommand(merchantIdStr, request);
        ProductOutboundResult result = commandBus.dispatch(command);
        return createProductMapper.toResponse(result);
    }

    public ProductResponse updateProduct(String merchantIdStr, String productIdStr, UpdateProductRequest request) {
        UpdateSellerProductCommand command = updateProductMapper.toCommand(merchantIdStr, productIdStr, request);
        ProductOutboundResult result = commandBus.dispatch(command);
        return updateProductMapper.toResponse(result);
    }

    public void deleteProduct(String merchantIdStr, String productIdStr) {
        DeleteSellerProductCommand command = deleteProductMapper.toCommand(merchantIdStr, productIdStr);
        commandBus.dispatch(command);
    }

    public ProductResponse updateProductStatus(String merchantIdStr, String productIdStr, UpdateProductStatusRequest request) {
        UpdateSellerProductStatusCommand command = updateProductStatusMapper.toCommand(merchantIdStr, productIdStr, request);
        ProductOutboundResult result = commandBus.dispatch(command);
        return updateProductStatusMapper.toResponse(result);
    }
}
