package com.catalog.application.port.inbound;

import com.catalog.application.command.SetVariantMediaCommand;
import com.catalog.application.command.SetVariantMediaResult;

public interface SetVariantMediaUseCase {
    SetVariantMediaResult execute(SetVariantMediaCommand command);
}
