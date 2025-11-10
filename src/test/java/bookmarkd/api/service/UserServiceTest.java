package bookmarkd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import bookmarkd.api.entity.User;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Test
    @Transactional
    void createUser_persistsTrimmedUsername() {
        TestDataUtil.clearDatabase();

        User created = userService.createUser("  alice  ");

        assertNotNull(created.id);
        assertEquals("alice", created.username);
    }

    @Test
    @Transactional
    void createUser_duplicateUsernameThrows() {
        TestDataUtil.clearDatabase();
        userService.createUser("bob");

        assertThrows(BadRequestException.class, () -> userService.createUser("bob"));
    }

    @Test
    @Transactional
    void listUsers_filtersByCaseInsensitiveSubstring() {
        TestDataUtil.clearDatabase();
        userService.createUser("alice");
        userService.createUser("alicia");
        userService.createUser("charlie");

        List<User> matches = userService.listUsers("lic", null, null);

        assertEquals(2, matches.size());
    }

    @Test
    @Transactional
    void listUsers_paginatesAlphabetically() {
        TestDataUtil.clearDatabase();
        userService.createUser("anna");
        userService.createUser("bella");
        userService.createUser("claire");

        List<User> firstPage = userService.listUsers(null, 1, 2);
        List<User> secondPage = userService.listUsers(null, 2, 2);

        assertEquals(2, firstPage.size());
        assertEquals("anna", firstPage.get(0).username);
        assertEquals("bella", firstPage.get(1).username);

        assertEquals(1, secondPage.size());
        assertEquals("claire", secondPage.get(0).username);
    }

    @Test
    @Transactional
    void updateUser_changesUsernameAndValidatesDuplicates() {
        TestDataUtil.clearDatabase();
        User bob = userService.createUser("bob");
        userService.createUser("carol");

        User updated = userService.updateUser(bob.id, "bobby");

        assertEquals("bobby", updated.username);
        assertThrows(BadRequestException.class, () -> userService.updateUser(updated.id, "carol"));
    }
}
