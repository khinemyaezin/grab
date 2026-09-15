package com.catalog.domain.service;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductMedia;
import com.grab.framework.id.Id;

import java.util.Collection;
import java.util.List;

public interface ProductMediaService {

    void replaceGallery(Product product, List<ProductMedia> medias);

    void assignVariantMedia(Product product, Id variantId, Collection<Id> mediaIds, Id thumbnailMediaId);
}
