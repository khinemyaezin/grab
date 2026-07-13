package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.mapper.ActivateBinRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.CreateBinRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.DeactivateBinRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.DeleteBinRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.UpdateBinRequestMapper;
import lombok.RequiredArgsConstructor;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BinCommandService {

    private final CommandBus commandBus;
    private final CreateBinRequestMapper createBinRequestMapper;
    private final UpdateBinRequestMapper updateBinRequestMapper;
    private final ActivateBinRequestMapper activateBinRequestMapper;
    private final DeactivateBinRequestMapper deactivateBinRequestMapper;
    private final DeleteBinRequestMapper deleteBinRequestMapper;

    public BinResponse createBin(CreateBinRequest request, ResolvedInventoryAccess access) {
        var command = createBinRequestMapper.toCommand(request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return createBinRequestMapper.toResponse(result);
    }

    public BinResponse updateBin(String binId, UpdateBinRequest request, ResolvedInventoryAccess access) {
        var command = updateBinRequestMapper.toCommand(binId, request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return updateBinRequestMapper.toResponse(result);
    }

    public BinResponse activateBin(String binId, ResolvedInventoryAccess access) {
        var command = activateBinRequestMapper.toCommand(binId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return activateBinRequestMapper.toResponse(result);
    }

    public BinResponse deactivateBin(String binId, ResolvedInventoryAccess access) {
        var command = deactivateBinRequestMapper.toCommand(binId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return deactivateBinRequestMapper.toResponse(result);
    }

    public void deleteBin(String binId, ResolvedInventoryAccess access) {
        var command = deleteBinRequestMapper.toCommand(binId, access.actorId(), access.scopeKey(), access.scopeId());
        commandBus.dispatch(command);
    }
}
