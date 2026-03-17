package com.catalog.domain.aggregate;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import lombok.Getter;

@Getter
public class ProductMedia extends Entity<Id> {
    private final String type;
    private final String path;

    public ProductMedia(Id id, String type, String path) {
        super(id);
        this.type = type;
        this.path = path;
    }
}
