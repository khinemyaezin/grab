package com.grab.app.account.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.ListMerchantMembersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListMerchantMembersQueryHandler implements QueryHandler<ListMerchantMembersQuery, List<MerchantMemberResult>> {

    private final ListMerchantMembersUseCase listMerchantMembersUseCase;

    @Override
    @MerchantReadTransactional
    public List<MerchantMemberResult> handle(ListMerchantMembersQuery query) {
        return listMerchantMembersUseCase.execute(query);
    }

    @Override
    public Class<ListMerchantMembersQuery> getQueryType() {
        return ListMerchantMembersQuery.class;
    }
}
