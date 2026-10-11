package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record PublishMerchantRoleDeclarationCommand() implements Command<Void> {
}
