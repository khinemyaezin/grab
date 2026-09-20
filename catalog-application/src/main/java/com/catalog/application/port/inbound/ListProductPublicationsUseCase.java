package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ListProductPublicationsQuery;
import com.catalog.application.model.read.ProductPublicationItem;
import java.util.List;

public interface ListProductPublicationsUseCase {
    List<ProductPublicationItem> execute(ListProductPublicationsQuery query);
}
