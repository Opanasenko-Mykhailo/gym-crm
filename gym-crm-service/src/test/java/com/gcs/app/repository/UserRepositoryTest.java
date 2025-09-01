package com.gcs.app.repository;

import com.gcs.app.model.User;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest extends AbstractRepositoryTest {

    private static final String USERNAME_EXISTING = "sofia.melnyk";
    private static final String USERNAME_NEW = "alex.ivanov";

    @Autowired
    private UserRepository userRepository;

    @Test
    @DataSet(value = "dataset/user-data.xml", cleanBefore = true, cleanAfter = true)
    void readUser_findByUsername_existingUser_returnsUser() {
        Optional<User> user = userRepository.findByUsername(USERNAME_EXISTING);

        assertTrue(user.isPresent());
        assertEquals(USERNAME_EXISTING, user.get().getUsername());
    }

    @Test
    void createUser_savesUserSuccessfully() {
        User user = createUser();
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals(USERNAME_NEW, saved.getUsername());
    }

    @Test
    void updateUser_updatesFirstNameSuccessfully() {
        User user = createUser();
        userRepository.save(user);

        User saved = userRepository.findByUsername(USERNAME_NEW).orElseThrow();
        User updated = saved.toBuilder().firstName("Alexander").build();
        userRepository.save(updated);

        User reloaded = userRepository.findByUsername(USERNAME_NEW).orElseThrow();
        assertEquals("Alexander", reloaded.getFirstName());
    }

    @Test
    void deleteUser_removesUserSuccessfully() {
        User user = createUser();
        userRepository.save(user);

        User saved = userRepository.findByUsername(USERNAME_NEW).orElseThrow();
        userRepository.delete(saved);

        Optional<User> afterDelete = userRepository.findByUsername(USERNAME_NEW);
        assertFalse(afterDelete.isPresent());
    }

    @Test
    @DataSet(value = "dataset/user-data.xml", cleanBefore = true, cleanAfter = true)
    void findAllUsernames_returnsAllUsernames() {
        Set<String> usernames = userRepository.findAllUsernames();

        assertNotNull(usernames);
        assertTrue(usernames.contains(USERNAME_EXISTING));
    }

    private User createUser() {
        return User.builder()
                .username(USERNAME_NEW)
                .password("password")
                .firstName("Alex")
                .lastName("Ivanov")
                .isActive(true)
                .build();
    }
}
