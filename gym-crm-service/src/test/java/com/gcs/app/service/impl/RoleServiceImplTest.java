package com.gcs.app.service.impl;

import com.gcs.app.exception.ServiceException;
import com.gcs.app.model.Role;
import com.gcs.app.model.enums.RoleType;
import com.gcs.app.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void getByType_whenRoleExists_returnsRole() {
        RoleType roleType = RoleType.ROLE_TRAINEE;
        Role expected = new Role(1L, roleType);
        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.of(expected));

        Role actual = roleService.getByType(roleType);

        assertEquals(expected, actual);
    }

    @Test
    void getByType_whenRoleDoesNotExist_throwsServiceException() {
        RoleType roleType = RoleType.ROLE_TRAINEE;
        when(roleRepository.findByRoleType(roleType)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> roleService.getByType(roleType));

        assertEquals("Role not found: " + roleType, exception.getMessage());
    }
}