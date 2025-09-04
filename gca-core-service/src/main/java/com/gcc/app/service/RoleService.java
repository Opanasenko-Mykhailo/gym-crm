package com.gcc.app.service;

import com.gcc.app.model.Role;
import com.gcc.app.model.enums.RoleType;

public interface RoleService {
    Role getByType(RoleType type);
}