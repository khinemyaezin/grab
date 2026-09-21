package com.inventory.application.port.inbound;

import com.inventory.application.model.read.ListSkusAtLocationQuery;

import java.util.List;

public interface ListSkusAtLocationUseCase {
    List<String> execute(ListSkusAtLocationQuery query);
}
