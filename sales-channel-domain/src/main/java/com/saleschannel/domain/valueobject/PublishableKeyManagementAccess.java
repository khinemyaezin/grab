package com.saleschannel.domain.valueobject;

import com.grab.framework.id.Id;

public record PublishableKeyManagementAccess(Id merchantId, boolean administrator) {
}
