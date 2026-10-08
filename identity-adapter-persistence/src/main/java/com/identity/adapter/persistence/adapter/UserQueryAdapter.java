package com.identity.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.identity.application.port.outbound.UserQueryPort;
import com.identity.application.model.read.UserAssignmentView;
import com.identity.application.model.read.UserListView;
import com.identity.adapter.persistence.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public List<UserAssignmentView> queryUserAndByUserId(String userId) {
        return executor.query("User", () -> userJpaRepository.queryUserAndByUserId(userId));
    }

    @Override
    public Page<UserListView> findAll(Pageable pageable) {
        return executor.query("User", () -> userJpaRepository.findAll(pageable)
                .map(user -> new UserListView(
                        user.getUuid(),
                        user.getEmail(),
                        user.getStatus().name(),
                        user.getCreatedAt().toString()
                )));
    }
}
