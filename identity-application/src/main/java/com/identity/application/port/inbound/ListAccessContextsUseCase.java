package com.identity.application.port.inbound;

import com.identity.application.model.read.ListAccessContextsQuery;
import java.util.List;
import com.identity.application.model.read.AccessContextResult;

public interface ListAccessContextsUseCase {
    List<AccessContextResult> execute(ListAccessContextsQuery query);
}
