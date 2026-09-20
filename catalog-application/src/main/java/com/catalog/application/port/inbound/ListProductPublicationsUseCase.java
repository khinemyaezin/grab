package com.catalog.application.port.inbound;

import com.catalog.application.query.ListProductPublicationsQuery;
import com.catalog.application.query.ProductPublicationItem;
import java.util.List;

public interface ListProductPublicationsUseCase {
    List<ProductPublicationItem> execute(ListProductPublicationsQuery query);
}
