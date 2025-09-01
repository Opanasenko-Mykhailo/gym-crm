package com.gcs.app.service.impl;

import com.gcs.app.exception.ServiceException;
import com.gcs.app.model.Role;
import com.gcs.app.model.enums.RoleType;
import com.gcs.app.repository.RoleRepository;
import com.gcs.app.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getByType(RoleType type) {
        return roleRepository.findByRoleType(type)
                .orElseThrow(() -> new ServiceException("Role not found: " + type));
    }
}