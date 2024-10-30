package dataaccess;

import model.*;
import org.junit.jupiter.api.*;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;


public class AuthDataAccessTests {
    private UserService userService;
    private AuthDataAccess authDataAccess;

    @BeforeEach
    void setup() {
        userService = new UserService();
        authDataAccess = userService.getAuthDataAccess();

        authDataAccess.clearAuths();
    }

    @Test
    @DisplayName("Test GetAuthInfoByUsername Positive")
    public void testGetAuthInfoByUsernamePositive() throws Exception {
        var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
        DatabaseManager.executeUpdate(statement, "username", "authtoken");

        AuthData auth = authDataAccess.getAuthInfoByUsername("username");

        assertEquals("username", auth.getUsername());
        assertEquals("authtoken", auth.getAuthToken());
    }

    @Nested
    @DisplayName("Test GetAuthInfoByUsername Negative")
    class TestGetAuthInfoByUsernameNegative {

        @Test
        @DisplayName("Invalid Username")
        void testGetAuthInfoByUsernameInvalidUsername() throws Exception {
            var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
            DatabaseManager.executeUpdate(statement, "username", "authtoken");

            AuthData auth = authDataAccess.getAuthInfoByUsername("fakeusername");

            assertNull(auth);
        }

        @Test
        @DisplayName("Null Username Given")
        void testGetAuthInfoByUsernameNullUsername() throws Exception {
            var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
            DatabaseManager.executeUpdate(statement, "username", "authtoken");

            AuthData auth = authDataAccess.getAuthInfoByUsername(null);

            assertNull(auth);
        }
    }

    @Test
    @DisplayName("Test GetAuthInfoByToken")
    public void testGetAuthInfoByTokenPositive() throws Exception {
        var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
        DatabaseManager.executeUpdate(statement, "username", "authtoken");

        AuthData auth = authDataAccess.getAuthInfoByToken("authtoken");

        assertEquals("username", auth.getUsername());
        assertEquals("authtoken", auth.getAuthToken());
    }

    @Nested
    @DisplayName("Test GetAuthInfoByToken Negative")
    class TestGetAuthInfoByTokenNegative {

        @Test
        @DisplayName("Invalid Token")
        void testGetAuthInfoByUsernameInvalidUsername() throws Exception {
            var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
            DatabaseManager.executeUpdate(statement, "username", "authtoken");

            AuthData auth = authDataAccess.getAuthInfoByToken("faketoken");

            assertNull(auth);
        }

        @Test
        @DisplayName("Null Token Given")
        void testGetAuthInfoByUsernameNullUsername() throws Exception {
            var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
            DatabaseManager.executeUpdate(statement, "username", "authtoken");

            AuthData auth = authDataAccess.getAuthInfoByUsername(null);

            assertNull(auth);
        }
    }

    @Test
    @DisplayName("Test AddAuthToken Positive")
    public void testAddAuthTokenPositive() throws Exception {
        authDataAccess.addAuthToken("username", "authtoken");
        authDataAccess.addAuthToken("username2", "authtoken2");
        authDataAccess.addAuthToken("username3", "authtoken3");

        // Since we already know that getAuthInfoByUsername works
        AuthData auth = authDataAccess.getAuthInfoByUsername("username");
        assertEquals("username", auth.getUsername());
        assertEquals("authtoken", auth.getAuthToken());

        auth = authDataAccess.getAuthInfoByUsername("username2");
        assertEquals("username2", auth.getUsername());
        assertEquals("authtoken2", auth.getAuthToken());

        auth = authDataAccess.getAuthInfoByUsername("username3");
        assertEquals("username3", auth.getUsername());
        assertEquals("authtoken3", auth.getAuthToken());

        assertEquals(3, authDataAccess.getAllAuths().size());
    }

    @Nested
    @DisplayName("Test AddAuthToken Negative")
    class TestAddAuthTokenNegative {

        @Test
        @DisplayName("Null Username Given")
        void testAddAuthTokenNullUsername() throws Exception {
            DataAccessException exception = assertThrows(DataAccessException.class, () -> {
                authDataAccess.addAuthToken(null, "authtoken");
            });

            assertEquals(500, exception.statusCode());
            assertEquals("unable to update database: INSERT INTO auths (username, token) VALUES (?, ?), Column 'username' cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Null Token Given")
        void testAddAuthTokenNullToken() throws Exception {
            DataAccessException exception = assertThrows(DataAccessException.class, () -> {
                authDataAccess.addAuthToken("username", null);
            });

            assertEquals(500, exception.statusCode());
            assertEquals("unable to update database: INSERT INTO auths (username, token) VALUES (?, ?), Column 'token' cannot be null", exception.getMessage());
        }

    }

    @Test
    @DisplayName("Test RemoveAuthorization Positive")
    public void testRemoveAuthorizationPositive() throws Exception {
        authDataAccess.addAuthToken("username", "authtoken");
        AuthData auth = authDataAccess.getAuthInfoByUsername("username");
        assertEquals("username", auth.getUsername());
        assertEquals("authtoken", auth.getAuthToken());

        authDataAccess.removeAuthorization("authtoken");
        auth = authDataAccess.getAuthInfoByUsername("username");
        assertNull(auth);
    }

    @Test
    @DisplayName("Test RemoveAuthorization Negative")
    public void testRemoveAuthorizationNegative() throws Exception {
        authDataAccess.addAuthToken("username", "authtoken");
        authDataAccess.addAuthToken("username2", "authtoken2");
        authDataAccess.addAuthToken("username3", "authtoken3");

        authDataAccess.removeAuthorization(null);

        assertNotNull(authDataAccess.getAuthInfoByUsername("username"));
        assertNotNull(authDataAccess.getAuthInfoByUsername("username2"));
        assertNotNull(authDataAccess.getAuthInfoByUsername("username3"));
    }

    @Test
    @DisplayName("Test ClearAuths Positive")
    public void testClearAuthsPositive() throws Exception {
        authDataAccess.addAuthToken("username", "authtoken");
        authDataAccess.addAuthToken("username2", "authtoken2");
        authDataAccess.addAuthToken("username3", "authtoken3");

        assertEquals(3, authDataAccess.getAllAuths().size());

        authDataAccess.clearAuths();

        assertNull(authDataAccess.getAuthInfoByUsername("username"));
        assertNull(authDataAccess.getAuthInfoByUsername("username2"));
        assertNull(authDataAccess.getAuthInfoByUsername("username3"));
        assertEquals(0, authDataAccess.getAllAuths().size());
    }

    // Get All Auths only used in tests so no test case written

}