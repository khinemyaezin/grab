package com.grab.store.merchant.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.MerchantAccountResult;

import java.util.List;

public record ListMyMerchantsQuery(Id applicantUserId) implements Query<List<MerchantAccountResult>> {
}
