package com.gcs.app.service;

import com.gcs.app.model.Role;
import com.gcs.app.model.enums.RoleType;

public interface RoleService {
    Role getByType(RoleType type);
}