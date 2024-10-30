package dataaccess;

import model.*;
import org.junit.jupiter.api.*;
import service.UserService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class UserDataAccessTests {
    private UserService userService;
    private UserDataAccess userDataAccess;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserService();
        userDataAccess = userService.getUserDataAccess();

        userDataAccess.clearUsers();
    }

    @Test
    @DisplayName("Test GetUser Success")
    void testGetUserSuccess() throws Exception {
        userDataAccess.addUser(new UserData("username", "password", "email"), "hashedpassword");

        UserData user = userDataAccess.getUser("username");

        assertEquals("hashedpassword", user.getPassword());
        assertEquals("username", user.getUsername());
        assertEquals("email", user.getEmail());
    }

    @Test
    @DisplayName("Test GetUser Failure")
    void testGetUserFailure() throws Exception {
        userDataAccess.addUser(new UserData("username", "password", "email"), "hashedpassword");

        assertNull(userDataAccess.getUser("user_that_does_not_exist"));
    }

    // List users only used in tests so no tests written

    @Test
    @DisplayName("Test AddUser Success")
    void testAddUserSuccess() throws Exception {
        userDataAccess.addUser(new UserData("username", "password", "email"), "hashedpassword");

        try {
            var conn = DatabaseManager.getConnection();
            var statement = "SELECT * FROM games WHERE username='username';";
            var ps = conn.prepareStatement(statement);
            var rs = ps.executeQuery();
            if (rs.next()) {
                UserData user = readTheUser(ps.executeQuery());

                assertEquals("gameName", user.username());
                assertEquals("hashedpassword", user.password());
                assertEquals("email", user.email());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test AddUser Failure")
    void testAddUserFailure() throws Exception {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDataAccess.addUser(new UserData(null, "password", "email"), "hashedpassword");
        });
        assertEquals(500, exception.statusCode());
        assertEquals("unable to update database: INSERT INTO users (username, password, email) VALUES (?, ?, ?), Column 'username' " +
                "cannot be null", exception.getMessage());

        exception = assertThrows(DataAccessException.class, () -> {
            userDataAccess.addUser(new UserData("username", "password", "email"), null);
        });
        assertEquals(500, exception.statusCode());
        assertEquals("unable to update database: INSERT INTO users (username, password, email) VALUES (?, ?, ?), Column 'password' " +
                "cannot be null", exception.getMessage());

        exception = assertThrows(DataAccessException.class, () -> {
            userDataAccess.addUser(new UserData("username", "password", null), "hashedpassword");
        });
        assertEquals(500, exception.statusCode());
        assertEquals("unable to update database: INSERT INTO users (username, password, email) VALUES (?, ?, ?), Column 'email' " +
                "cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Test ClearUsers Success")
    void testClearUsersSuccess() throws Exception {
        userDataAccess.addUser(new UserData("username", "password", "email"), "hashedpassword");
        userDataAccess.addUser(new UserData("username2", "password", "email"), "hashedpassword");
        userDataAccess.addUser(new UserData("username3", "password", "email"), "hashedpassword");

        userDataAccess.clearUsers();

        Collection<UserData> users = userDataAccess.listUsers();
        assertEquals(0, users.size());
    }

    // ------ HELPER FUNCTIONS ------ //

    private UserData readTheUser(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        return new UserData(username, password, email);
    }
}