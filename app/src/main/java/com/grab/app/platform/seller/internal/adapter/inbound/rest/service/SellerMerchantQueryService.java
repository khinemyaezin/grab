package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerMerchantMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerStorefrontMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListMySellerMerchantsMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerMerchantMembersMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerStorefrontsMapper;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetSellerMerchantQuery;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.GetSellerStorefrontQuery;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListMySellerMerchantsQuery;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListSellerMerchantMembersQuery;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListSellerStorefrontsQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerMerchantQueryService {

    private final QueryBus queryBus;
    private final GetSellerMerchantMapper getMerchantMapper;
    private final ListMySellerMerchantsMapper listMyMerchantsMapper;
    private final GetSellerStorefrontMapper getStorefrontMapper;
    private final ListSellerStorefrontsMapper listStorefrontsMapper;
    private final ListSellerMerchantMembersMapper listMembersMapper;

    public SellerMerchantQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerMerchantMapper getMerchantMapper,
            ListMySellerMerchantsMapper listMyMerchantsMapper,
            GetSellerStorefrontMapper getStorefrontMapper,
            ListSellerStorefrontsMapper listStorefrontsMapper,
            ListSellerMerchantMembersMapper listMembersMapper
    ) {
        this.queryBus = queryBus;
        this.getMerchantMapper = getMerchantMapper;
        this.listMyMerchantsMapper = listMyMerchantsMapper;
        this.getStorefrontMapper = getStorefrontMapper;
        this.listStorefrontsMapper = listStorefrontsMapper;
        this.listMembersMapper = listMembersMapper;
    }

    public Optional<MerchantResponse> getMerchant(String merchantIdStr, String actorIdStr) {
        GetSellerMerchantQuery query = getMerchantMapper.toQuery(merchantIdStr, actorIdStr);
        Optional<MerchantOutboundResult> res = queryBus.dispatch(query);
        return res.map(getMerchantMapper::toResponse);
    }

    public List<MerchantResponse> listMyMerchants(String applicantUserIdStr) {
        ListMySellerMerchantsQuery query = listMyMerchantsMapper.toQuery(applicantUserIdStr);
        List<MerchantOutboundResult> results = queryBus.dispatch(query);
        return listMyMerchantsMapper.toResponse(results);
    }

    public Optional<StorefrontResponse> getStorefront(String merchantIdStr, String storefrontIdStr) {
        GetSellerStorefrontQuery query = getStorefrontMapper.toQuery(merchantIdStr, storefrontIdStr);
        Optional<StorefrontOutboundResult> res = queryBus.dispatch(query);
        return res.map(getStorefrontMapper::toResponse);
    }

    public List<StorefrontResponse> listStorefronts(String merchantIdStr) {
        ListSellerStorefrontsQuery query = listStorefrontsMapper.toQuery(merchantIdStr);
        List<StorefrontOutboundResult> results = queryBus.dispatch(query);
        return listStorefrontsMapper.toResponse(results);
    }

    public List<MerchantMemberResponse> listMembers(String merchantIdStr) {
        ListSellerMerchantMembersQuery query = listMembersMapper.toQuery(merchantIdStr);
        List<MerchantMemberOutboundResult> results = queryBus.dispatch(query);
        return listMembersMapper.toResponse(results);
    }
}
