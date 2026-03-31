package com.catalog.domain.service.dto;

import com.grab.framework.id.Id;

import java.util.List;

public record VariantTypeSelection (
    Id typeId,
    List<VariantOptionSelection> options
){
}
