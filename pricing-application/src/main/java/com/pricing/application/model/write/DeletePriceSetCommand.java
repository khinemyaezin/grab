package com.pricing.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeletePriceSetCommand(Id priceSetId) implements Command<Void> {
}
