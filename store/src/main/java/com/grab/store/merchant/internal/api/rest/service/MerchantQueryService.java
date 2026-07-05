package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.merchant.internal.api.rest.dto.response.GetC2CApplicationResponse;
import com.grab.store.merchant.internal.api.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.api.rest.mapper.GetC2CProfileRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.GetFirstPartyRetailerApplicationRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.GetMerchantRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.ListMerchantReviewQueueRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.ListMyMerchantsRequestMapper;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.query.*;
import com.grab.store.shared.security.SecurityPrincipal;
import com.grab.store.merchant.internal.api.rest.config.MerchantAuthorityCodes;
import com.merchant.domain.enums.MerchantStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantQueryService {
    private final QueryBus queries;
    private final GetMerchantRequestMapper getMapper;
    private final ListMyMerchantsRequestMapper listMineMapper;
    private final ListMerchantReviewQueueRequestMapper reviewQueueMapper;
    private final GetC2CProfileRequestMapper getC2CApplicationMapper;
    private final GetFirstPartyRetailerApplicationRequestMapper getFirstPartyRetailerApplicationMapper;
    private final AuthenticatedMerchantScopeResolver merchantScopes;

    public MerchantResponse get(String merchantId, SecurityPrincipal principal) {
        boolean reviewer = principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(MerchantAuthorityCodes.GLOBAL_READ));
        boolean scopedAccess = merchantScopes.resolveScopedAccess(principal, merchantId, reviewer);
        String actorId = principal.getPlatformUserId();
        GetMerchantQuery query = getMapper.toQuery(merchantId, actorId, reviewer, scopedAccess);
        MerchantAccountResult result = queries.dispatch(query);
        return getMapper.toResponse(result);
    }

    public MerchantResponse current(SecurityPrincipal principal) {
        String merchantId = merchantScopes.resolveCurrentMerchantId(principal);
        return get(merchantId, principal);
    }


    public List<MerchantResponse> mine(String applicantId) {
        ListMyMerchantsQuery query = listMineMapper.toQuery(applicantId);
        List<MerchantAccountResult> results = queries.dispatch(query);
        return listMineMapper.toResponse(results);
    }

    public List<MerchantResponse> reviewQueue(MerchantStatus status) {
        ListMerchantReviewQueueQuery query = reviewQueueMapper.toQuery(status);
        List<MerchantAccountResult> results = queries.dispatch(query);
        return reviewQueueMapper.toResponse(results);
    }

    public GetC2CApplicationResponse getC2CApplication(String applicantUserId) {
        GetC2CApplicationQuery query = getC2CApplicationMapper.toQuery(applicantUserId);
        GetC2CApplicationResult view = queries.dispatch(query);
        return getC2CApplicationMapper.toResponse(view);
    }

    public GetFirstPartyRetailerApplicationResponse getFirstPartyRetailerApplication(String applicantUserId) {
        GetFirstPartyRetailerApplicationQuery query = getFirstPartyRetailerApplicationMapper.toQuery(applicantUserId);
        GetFirstPartyRetailerApplicationResult result = queries.dispatch(query);
        return getFirstPartyRetailerApplicationMapper.toResponse(result);
    }
}
