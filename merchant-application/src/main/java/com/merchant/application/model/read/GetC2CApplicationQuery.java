package com.merchant.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetC2CApplicationQuery(Id applicantUserId) implements Query<GetC2CApplicationResult> {
}
