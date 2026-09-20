package com.merchant.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.merchant.application.model.write.MerchantAccountResult;

import java.util.List;

public record ListMyMerchantsQuery(Id applicantUserId) implements Query<List<MerchantAccountResult>> {
}
