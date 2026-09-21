package com.catalog.application.port.outbound;

import com.catalog.application.model.read.VariantTypeView;

import java.util.List;

public interface VariantTypeQueryPort {
    List<VariantTypeView> findByName(String name);
}
