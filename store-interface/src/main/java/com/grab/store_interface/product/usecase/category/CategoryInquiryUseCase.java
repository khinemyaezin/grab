package com.grab.store_interface.product.usecase.category;


import com.grab.store_interface.product.dto.category.ReadableCategory;

import java.util.Optional;

public interface CategoryInquiryUseCase {
    Optional<ReadableCategory> findImmediateSubordinatesOf(String id) ;

    Optional<ReadableCategory> findChildrenOf(String id);

    Optional<ReadableCategory> findParentOf(String id) ;
}
