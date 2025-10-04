package com.gcc.app.service.impl;

import com.gcc.app.exception.EntityNotFoundException;
import com.gcc.app.model.Role;
import com.gcc.app.model.enums.RoleType;
import com.gcc.app.repository.RoleRepository;
import com.gcc.app.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getByType(RoleType type) {
        return roleRepository.findByRoleType(type)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + type));
    }
}