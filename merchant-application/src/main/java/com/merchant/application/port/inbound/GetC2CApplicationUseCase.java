package com.merchant.application.port.inbound;

import com.merchant.application.model.read.GetC2CApplicationQuery;
import com.merchant.application.model.read.GetC2CApplicationResult;

public interface GetC2CApplicationUseCase {
    GetC2CApplicationResult execute(GetC2CApplicationQuery query);
}
