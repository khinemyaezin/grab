package com.merchant.application.port.inbound;

import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.MerchantMemberResult;

import java.util.List;

public interface ListMerchantMembersUseCase {
    List<MerchantMemberResult> execute(ListMerchantMembersQuery query);
}
