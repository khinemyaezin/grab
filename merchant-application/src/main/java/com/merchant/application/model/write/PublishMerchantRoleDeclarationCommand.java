package com.merchant.application.model.write;

import com.grab.framework.security.role.RoleDeclarationPublicationPort.PublicationResult;
import com.grab.framework.cqrs.command.Command;

public record PublishMerchantRoleDeclarationCommand() implements Command<PublicationResult> {
}
