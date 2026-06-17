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

    public BinResponse createBin(CreateBinRequest request, String actorId) {
        var command = createBinRequestMapper.toCommand(request, actorId);
        var result = commandBus.dispatch(command);
        return createBinRequestMapper.toResponse(result);
    }

    public BinResponse updateBin(String binId, UpdateBinRequest request, String actorId) {
        var command = updateBinRequestMapper.toCommand(binId, request, actorId);
        var result = commandBus.dispatch(command);
        return updateBinRequestMapper.toResponse(result);
    }

    public BinResponse activateBin(String binId, String actorId) {
        var command = activateBinRequestMapper.toCommand(binId, actorId);
        var result = commandBus.dispatch(command);
        return activateBinRequestMapper.toResponse(result);
    }

    public BinResponse deactivateBin(String binId, String actorId) {
        var command = deactivateBinRequestMapper.toCommand(binId, actorId);
        var result = commandBus.dispatch(command);
        return deactivateBinRequestMapper.toResponse(result);
    }

    public void deleteBin(String binId, String actorId) {
        var command = deleteBinRequestMapper.toCommand(binId, actorId);
        commandBus.dispatch(command);
    }
}
