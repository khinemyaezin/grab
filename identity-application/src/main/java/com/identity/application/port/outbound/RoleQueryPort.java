package com.identity.application.port.outbound;

import com.identity.application.model.read.RoleListView;
import com.identity.application.model.read.RoleView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleQueryPort {
    List<RoleView> queryByName(String name);

    Page<RoleListView> findAll(Pageable pageable);
}
