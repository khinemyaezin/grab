package com.merchant.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.domain.enums.MerchantStatus;

import java.util.List;

public record ListMerchantReviewQueueQuery(MerchantStatus status) implements Query<List<MerchantAccountResult>> {
}
