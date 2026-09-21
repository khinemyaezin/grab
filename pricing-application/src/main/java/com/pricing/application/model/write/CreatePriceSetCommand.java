package com.pricing.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record CreatePriceSetCommand() implements Command<CreatePriceSetResult> {
}
