package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record PublishMerchantSecurityManifestCommand() implements Command<Void> {
}
