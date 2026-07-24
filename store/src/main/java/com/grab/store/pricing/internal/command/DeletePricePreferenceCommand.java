package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeletePricePreferenceCommand(Id pricePreferenceId) implements Command<Void> {
}
