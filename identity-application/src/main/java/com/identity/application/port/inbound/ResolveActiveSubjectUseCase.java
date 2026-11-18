package com.identity.application.port.inbound;

import com.identity.application.model.read.ActiveSubjectView;
import com.identity.application.model.read.ResolveActiveSubjectQuery;

public interface ResolveActiveSubjectUseCase {
    ActiveSubjectView execute(ResolveActiveSubjectQuery query);
}
