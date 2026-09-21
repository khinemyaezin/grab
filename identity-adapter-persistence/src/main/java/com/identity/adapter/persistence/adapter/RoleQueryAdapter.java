package com.identity.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.identity.application.port.outbound.RoleQueryPort;
import com.identity.application.model.read.RoleListView;
import com.identity.application.model.read.RoleNameView;
import com.identity.application.model.read.RoleView;
import com.identity.adapter.persistence.entity.AuthorityEntity;
import com.identity.adapter.persistence.repository.jpa.PlatformJpaRepository;
import com.identity.adapter.persistence.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RoleQueryAdapter implements RoleQueryPort {

    private final RoleJpaRepository jpaRepository;
    private final PlatformJpaRepository platformJpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public List<RoleView> queryByName(String name) {
        return executor.query("Role", () -> jpaRepository.findTop5ByNameStartingWithIgnoreCase(name).stream()
                .<RoleView>map(role -> new RoleNameView(role.getId(), role.getName(), role.getCode()))
                .toList());
    }

    @Override
    public Page<RoleListView> findAll(Pageable pageable) {
        return executor.query("Role", () -> jpaRepository.findAll(pageable).map(role -> new RoleListView(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getKind().name(),
                role.isActive(),
                role.isAssignable(),
                role.getAuthorities().stream()
                        .map(AuthorityEntity::getCode)
                        .collect(Collectors.toSet()),
                platformCodesFor(role.getCode())
        )));
    }

    private Set<String> platformCodesFor(String roleCode) {
        return platformJpaRepository.findDistinctByPlatformRoles_Role_CodeAndPlatformRoles_ActiveTrue(roleCode)
                .stream()
                .map(platform -> platform.getCode())
                .collect(Collectors.toSet());
    }
}
