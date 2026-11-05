package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListSellerMerchantMembersQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerMerchantMembersQueryHandler
        implements QueryHandler<ListSellerMerchantMembersQuery, List<MerchantMemberOutboundResult>> {

    private final SellerMerchantOutboundPort outboundPort;

    public ListSellerMerchantMembersQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<MerchantMemberOutboundResult> handle(ListSellerMerchantMembersQuery query) {
        return outboundPort.listMembers(query.merchantId());
    }

    @Override
    public Class<ListSellerMerchantMembersQuery> getQueryType() {
        return ListSellerMerchantMembersQuery.class;
    }
}

