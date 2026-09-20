package com.catalog.application.port.outbound;

import com.catalog.application.readmodel.VariantOptionView;

import java.util.List;

public interface VariantOptionQueryPort {
    List<VariantOptionView> findAllByUuidIn(List<String> uuids);

    List<VariantOptionView> findByNameAndTypeId(String name, String typeUuid);
}
