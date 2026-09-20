package com.identity.application.port.inbound;

import com.identity.application.model.read.ListAccessAssignmentsQuery;
import java.util.List;
import com.identity.application.model.write.AccessAssignmentResult;

public interface ListAccessAssignmentsUseCase {
    List<AccessAssignmentResult> execute(ListAccessAssignmentsQuery query);
}
