package com.identity.application.port.outbound;

import com.identity.application.model.read.UserAssignmentView;
import com.identity.application.model.read.UserListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserQueryPort {
    List<UserAssignmentView> queryUserAndByUserId(String userId);

    Page<UserListView> findAll(Pageable pageable);
}
