package com.grab.store.product.internal.util;

import com.grab.framework.cqrs.PageInfo;
import org.springframework.data.domain.Page;

public final class PageInfoMapper {
    public static PageInfo fromPage(
            Page<?> page
    ) {
        return new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
