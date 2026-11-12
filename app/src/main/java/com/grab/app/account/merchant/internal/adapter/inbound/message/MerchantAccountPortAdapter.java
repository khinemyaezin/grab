package com.grab.app.account.merchant.internal.adapter.inbound.message;

import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.ChangeMerchantLifecyclePortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.GetC2CApplicationPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.GetFirstPartyRetailerApplicationPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.GetMerchantPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.ListMerchantReviewQueuePortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.ListMyMerchantsPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.StartMerchantApplicationPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.SubmitMerchantApplicationPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.UpdateMerchantProfilePortMapper;
import com.grab.app.account.merchant.port.message.MerchantAccountPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.merchant.application.model.read.GetC2CApplicationQuery;
import com.merchant.application.model.read.GetC2CApplicationResult;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationQuery;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationResult;
import com.merchant.application.model.read.GetMerchantQuery;
import com.merchant.application.model.read.ListMerchantReviewQueueQuery;
import com.merchant.application.model.read.ListMyMerchantsQuery;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.model.write.StartMerchantApplicationCommand;
import com.merchant.application.model.write.SubmitMerchantApplicationCommand;
import com.merchant.application.model.write.UpdateMerchantProfileCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class MerchantAccountPortAdapter implements MerchantAccountPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final StartMerchantApplicationPortMapper startAppMapper;
    private final SubmitMerchantApplicationPortMapper submitAppMapper;
    private final ChangeMerchantLifecyclePortMapper lifecycleMapper;
    private final UpdateMerchantProfilePortMapper updateProfileMapper;
    private final GetMerchantPortMapper getMerchantMapper;
    private final ListMerchantReviewQueuePortMapper listReviewQueueMapper;
    private final ListMyMerchantsPortMapper listMyMerchantsMapper;
    private final GetC2CApplicationPortMapper getC2CAppMapper;
    private final GetFirstPartyRetailerApplicationPortMapper getFirstPartyAppMapper;

    public MerchantAccountPortAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("accountQueryBus") QueryBus queryBus,
            StartMerchantApplicationPortMapper startAppMapper,
            SubmitMerchantApplicationPortMapper submitAppMapper,
            ChangeMerchantLifecyclePortMapper lifecycleMapper,
            UpdateMerchantProfilePortMapper updateProfileMapper,
            GetMerchantPortMapper getMerchantMapper,
            ListMerchantReviewQueuePortMapper listReviewQueueMapper,
            ListMyMerchantsPortMapper listMyMerchantsMapper,
            GetC2CApplicationPortMapper getC2CAppMapper,
            GetFirstPartyRetailerApplicationPortMapper getFirstPartyAppMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.startAppMapper = startAppMapper;
        this.submitAppMapper = submitAppMapper;
        this.lifecycleMapper = lifecycleMapper;
        this.updateProfileMapper = updateProfileMapper;
        this.getMerchantMapper = getMerchantMapper;
        this.listReviewQueueMapper = listReviewQueueMapper;
        this.listMyMerchantsMapper = listMyMerchantsMapper;
        this.getC2CAppMapper = getC2CAppMapper;
        this.getFirstPartyAppMapper = getFirstPartyAppMapper;
    }

    @Override
    public MerchantMessageResult startApplication(StartMerchantApplicationMessage message) {
        StartMerchantApplicationCommand command = startAppMapper.toCommand(message);
        MerchantAccountResult result = commandBus.dispatch(command);
        return startAppMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult submitApplication(Id merchantId, Id applicantUserId) {
        SubmitMerchantApplicationCommand command = submitAppMapper.toCommand(merchantId, applicantUserId);
        MerchantAccountResult result = commandBus.dispatch(command);
        return submitAppMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult approveMerchant(Id merchantId, Id actorId) {
        ChangeMerchantLifecycleCommand command = lifecycleMapper.toCommand(
                merchantId, actorId, ChangeMerchantLifecycleCommand.Action.APPROVE, null
        );
        MerchantAccountResult result = commandBus.dispatch(command);
        return lifecycleMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult rejectMerchant(Id merchantId, Id actorId, String reason) {
        ChangeMerchantLifecycleCommand command = lifecycleMapper.toCommand(
                merchantId, actorId, ChangeMerchantLifecycleCommand.Action.REJECT, reason
        );
        MerchantAccountResult result = commandBus.dispatch(command);
        return lifecycleMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult requestChanges(Id merchantId, Id actorId, String comments) {
        ChangeMerchantLifecycleCommand command = lifecycleMapper.toCommand(
                merchantId, actorId, ChangeMerchantLifecycleCommand.Action.REQUEST_CHANGES, comments
        );
        MerchantAccountResult result = commandBus.dispatch(command);
        return lifecycleMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult suspendMerchant(Id merchantId, Id actorId, String reason) {
        ChangeMerchantLifecycleCommand command = lifecycleMapper.toCommand(
                merchantId, actorId, ChangeMerchantLifecycleCommand.Action.SUSPEND, reason
        );
        MerchantAccountResult result = commandBus.dispatch(command);
        return lifecycleMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult reactivateMerchant(Id merchantId, Id actorId) {
        ChangeMerchantLifecycleCommand command = lifecycleMapper.toCommand(
                merchantId, actorId, ChangeMerchantLifecycleCommand.Action.REACTIVATE, null
        );
        MerchantAccountResult result = commandBus.dispatch(command);
        return lifecycleMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult closeMerchant(Id merchantId, Id actorId) {
        ChangeMerchantLifecycleCommand command = lifecycleMapper.toCommand(
                merchantId, actorId, ChangeMerchantLifecycleCommand.Action.CLOSE, null
        );
        MerchantAccountResult result = commandBus.dispatch(command);
        return lifecycleMapper.toResponse(result);
    }

    @Override
    public MerchantMessageResult updateProfile(UpdateMerchantProfileMessage message) {
        UpdateMerchantProfileCommand command = updateProfileMapper.toCommand(message.merchantId(), message);
        MerchantAccountResult result = commandBus.dispatch(command);
        return updateProfileMapper.toResponse(result);
    }

    @Override
    public Optional<MerchantMessageResult> findMerchantById(Id merchantId, Id actorId, boolean reviewerAccess, boolean scopedAccess) {
        if (merchantId == null) {
            return Optional.empty();
        }
        GetMerchantQuery query = getMerchantMapper.toQuery(merchantId, actorId, reviewerAccess, scopedAccess);
        MerchantAccountResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getMerchantMapper.toResponse(result));
    }

    @Override
    public List<MerchantMessageResult> listMyMerchants(Id applicantUserId) {
        ListMyMerchantsQuery query = listMyMerchantsMapper.toQuery(applicantUserId);
        List<MerchantAccountResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listMyMerchantsMapper::toResponse).toList();
    }

    @Override
    public List<MerchantMessageResult> listReviewQueue(String status) {
        ListMerchantReviewQueueQuery query = listReviewQueueMapper.toQuery(status);
        List<MerchantAccountResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listReviewQueueMapper::toResponse).toList();
    }

    @Override
    public C2CApplicationResult getC2CApplication(Id applicantUserId) {
        GetC2CApplicationQuery query = getC2CAppMapper.toQuery(applicantUserId);
        GetC2CApplicationResult result = queryBus.dispatch(query);
        return getC2CAppMapper.toResponse(result);
    }

    @Override
    public FirstPartyApplicationResult getFirstPartyApplication(Id applicantUserId) {
        GetFirstPartyRetailerApplicationQuery query = getFirstPartyAppMapper.toQuery(applicantUserId);
        GetFirstPartyRetailerApplicationResult result = queryBus.dispatch(query);
        return getFirstPartyAppMapper.toResponse(result);
    }
}
