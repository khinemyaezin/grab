package com.catalog.application.port.outbound;

import com.catalog.application.readmodel.ProductHeroMediaView;
import com.catalog.application.readmodel.ProductPublicationView;
import com.catalog.application.readmodel.ProductSearchCriteria;
import com.catalog.application.readmodel.ProductVariantRefView;
import com.catalog.application.readmodel.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ProductQueryPort {
    Page<ProductView> search(ProductSearchCriteria criteria, Pageable pageable);

    List<ProductHeroMediaView> findHeroMediasByProductIds(Collection<String> productIds);

    List<ProductVariantRefView> findActiveVariantsByProductIds(Collection<String> productIds);

    List<ProductPublicationView> findPublicationsByProductIds(Collection<String> productIds);
}
