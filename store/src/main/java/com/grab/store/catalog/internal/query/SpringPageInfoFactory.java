package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.PageInfo;
import org.springframework.data.domain.Page;

public final class SpringPageInfoFactory {

    private SpringPageInfoFactory() {
    }

    public static PageInfo toPageInfo(Page<?> page) {
        return new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
