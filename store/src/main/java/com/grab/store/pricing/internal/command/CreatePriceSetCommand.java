package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;

public record CreatePriceSetCommand() implements Command<CreatePriceSetResult> {
}
