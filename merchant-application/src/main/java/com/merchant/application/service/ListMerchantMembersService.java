package com.merchant.application.service;

import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.port.inbound.ListMerchantMembersUseCase;
import com.merchant.application.port.outbound.MerchantMemberQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListMerchantMembersService implements ListMerchantMembersUseCase {
    private final MerchantMemberQueryPort queryPort;

    @Override
    public List<MerchantMemberResult> execute(ListMerchantMembersQuery query) {
        return queryPort.findByMerchantId(query.merchantId().getValue())
                .stream()
                .map(MerchantMemberResult::from)
                .toList();
    }
}
