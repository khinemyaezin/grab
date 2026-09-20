package com.catalog.application.port.inbound;

import com.catalog.application.model.write.SetVariantMediaCommand;
import com.catalog.application.model.write.SetVariantMediaResult;

public interface SetVariantMediaUseCase {
    SetVariantMediaResult execute(SetVariantMediaCommand command);
}
