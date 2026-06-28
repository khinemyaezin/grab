package com.grab.store.merchant.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.merchant.domain.enums.MerchantStatus;

import java.util.List;

public record ListMerchantReviewQueueQuery(MerchantStatus status) implements Query<List<MerchantAccountResult>> {
}
