package com.saleschannel.application.port.inbound;

import com.saleschannel.application.model.read.ListPublishableKeysQuery;
import com.saleschannel.application.model.read.PublishableKeyView;
import org.springframework.data.domain.Page;

public interface ListPublishableKeysUseCase {
    Page<PublishableKeyView> execute(ListPublishableKeysQuery query);
}
