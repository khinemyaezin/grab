package com.grab.app.account.merchant.internal.adapter.inbound.message;

import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.ChangeStorefrontLifecyclePortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.CreateStorefrontPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.GetStorefrontPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.ListStorefrontsByMerchantPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.UpdateStorefrontProfilePortMapper;
import com.grab.app.account.merchant.port.message.MerchantStorefrontPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.merchant.application.model.read.GetStorefrontQuery;
import com.merchant.application.model.read.ListStorefrontsByMerchantQuery;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand;
import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.model.write.UpdateStorefrontProfileCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class MerchantStorefrontPortAdapter implements MerchantStorefrontPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateStorefrontPortMapper createStorefrontMapper;
    private final UpdateStorefrontProfilePortMapper updateProfileMapper;
    private final ChangeStorefrontLifecyclePortMapper lifecycleMapper;
    private final GetStorefrontPortMapper getStorefrontMapper;
    private final ListStorefrontsByMerchantPortMapper listStorefrontsMapper;

    public MerchantStorefrontPortAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("accountQueryBus") QueryBus queryBus,
            CreateStorefrontPortMapper createStorefrontMapper,
            UpdateStorefrontProfilePortMapper updateProfileMapper,
            ChangeStorefrontLifecyclePortMapper lifecycleMapper,
            GetStorefrontPortMapper getStorefrontMapper,
            ListStorefrontsByMerchantPortMapper listStorefrontsMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createStorefrontMapper = createStorefrontMapper;
        this.updateProfileMapper = updateProfileMapper;
        this.lifecycleMapper = lifecycleMapper;
        this.getStorefrontMapper = getStorefrontMapper;
        this.listStorefrontsMapper = listStorefrontsMapper;
    }

    @Override
    public StorefrontMessageResult createStorefront(CreateStorefrontMessage message) {
        CreateStorefrontCommand command = createStorefrontMapper.toCommand(message);
        StorefrontResult result = commandBus.dispatch(command);
        return createStorefrontMapper.toResponse(result);
    }

    @Override
    public StorefrontMessageResult updateStorefront(Id storefrontId, Id merchantId, UpdateStorefrontMessage message) {
        UpdateStorefrontProfileCommand command = updateProfileMapper.toCommand(
                storefrontId, merchantId, message
        );
        StorefrontResult result = commandBus.dispatch(command);
        return updateProfileMapper.toResponse(result);
    }

    @Override
    public void activateStorefront(Id storefrontId, Id merchantId) {
        ChangeStorefrontLifecycleCommand command = lifecycleMapper.toCommand(
                storefrontId, merchantId, ChangeStorefrontLifecycleCommand.Action.ACTIVATE, null
        );
        commandBus.dispatch(command);
    }

    @Override
    public void suspendStorefront(Id storefrontId, Id merchantId, String reason) {
        ChangeStorefrontLifecycleCommand command = lifecycleMapper.toCommand(
                storefrontId, merchantId, ChangeStorefrontLifecycleCommand.Action.SUSPEND, reason
        );
        commandBus.dispatch(command);
    }

    @Override
    public void reactivateStorefront(Id storefrontId, Id merchantId) {
        ChangeStorefrontLifecycleCommand command = lifecycleMapper.toCommand(
                storefrontId, merchantId, ChangeStorefrontLifecycleCommand.Action.REACTIVATE, null
        );
        commandBus.dispatch(command);
    }

    @Override
    public void closeStorefront(Id storefrontId, Id merchantId) {
        ChangeStorefrontLifecycleCommand command = lifecycleMapper.toCommand(
                storefrontId, merchantId, ChangeStorefrontLifecycleCommand.Action.CLOSE, null
        );
        commandBus.dispatch(command);
    }

    @Override
    public Optional<StorefrontMessageResult> findStorefrontById(Id storefrontId, Id merchantId) {
        if (storefrontId == null) {
            return Optional.empty();
        }
        GetStorefrontQuery query = getStorefrontMapper.toQuery(storefrontId, merchantId);
        StorefrontResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getStorefrontMapper.toResponse(result));
    }

    @Override
    public List<StorefrontMessageResult> listStorefrontsByMerchant(Id merchantId) {
        ListStorefrontsByMerchantQuery query = listStorefrontsMapper.toQuery(merchantId);
        List<StorefrontResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listStorefrontsMapper::toResponse).toList();
    }
}
