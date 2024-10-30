package service;

import model.*;
import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

public class UserServiceTests {
    private UserService userService;
    private UserDataAccess userDataAccess;
    private AuthDataAccess authDataAccess;

    @BeforeEach
    void setup() throws Exception {
        userService = new UserService();
        userDataAccess = userService.getUserDataAccess();
        authDataAccess = userService.getAuthDataAccess();
        GameService gameService = new GameService(userService);

        gameService.clearDatabase();
    }

    @Test
    @DisplayName("Register User Success")
    void testRegisterUser() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData response = userService.registerUser(newUser);
        UserData checkUser = userService.getUserOnUserName(newUser.username());
        AuthData checkAuth = userService.getAuthInfoByUsername(newUser.username());

        assertEquals(newUser.getUsername(), response.getUsername(),
                "Test RegisterUser response did not return same username as given user.");
        assertNotNull(response.authToken(),
                "Test RegisterUser response did not return an authorization token.");

        assertEquals(newUser.username(), checkUser.username());
        assertEquals(newUser.email(), checkUser.email());
        assertNotNull(checkAuth.authToken());
        assertNotNull(checkAuth);
    }

    @Nested
    @DisplayName("Register User Failure")
    class TestRegisterUserBadRequest {

        @Test
        @DisplayName("No Username")
        public void testRegisterUserBadRequestBadUsername() throws Exception {
            UserData newUser = new UserData(null, "password", "test@email.com");
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            Collection<UserData> users = getAllUsers();
            Collection<AuthData> auths = getAllAuths();

            assertEquals(400, exception.statusCode());
            assertEquals("Error: bad request", exception.getMessage());
            assertTrue(users.isEmpty());
            assertTrue(auths.isEmpty());
        }

        @Test
        @DisplayName("No Password")
        public void testRegisterUserBadRequestBadPassword() throws Exception {
            UserData newUser = new UserData("username", null, "test@email.com");
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            Collection<UserData> users = getAllUsers();
            Collection<AuthData> auths = getAllAuths();

            assertEquals(400, exception.statusCode());
            assertEquals("Error: bad request", exception.getMessage());
            assertTrue(users.isEmpty());
            assertTrue(auths.isEmpty());
        }

        @Test
        @DisplayName("No Email")
        public void testRegisterUserBadRequestBadEmail() throws Exception {
            UserData newUser = new UserData("username", "password", null);
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            Collection<UserData> users = getAllUsers();
            Collection<AuthData> auths = getAllAuths();

            assertEquals(400, exception.statusCode());
            assertEquals("Error: bad request", exception.getMessage());
            assertTrue(users.isEmpty());
            assertTrue(auths.isEmpty());
        }
    }

    @Test
    @DisplayName("Login User Success")
    public void testLoginSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);
        userService.logoutUser(auth.authToken());
        AuthData authorization = userService.loginUser(newUser);
        Collection<AuthData> auths = getAllAuths();
        UserData user = userService.getUserOnUserName(newUser.username());

        assertNotNull(authorization);
        assertEquals(auths.size(), 1);

        newUser = new UserData("username1", "password", "test@email.com");
        auth = userService.registerUser(newUser);
        userService.logoutUser(auth.authToken());
        authorization = userService.loginUser(newUser);
        auths = getAllAuths();
        user = userService.getUserOnUserName(newUser.username());

        assertNotNull(authorization);
        assertEquals(auths.size(), 2);
    }

    @Nested
    @DisplayName("Login User Failure")
    class TestLoginFailure {

        @Test
        @DisplayName("No Matching User Info")
        public void testLoginFailureNoUser() throws Exception {
            UserData newUser = new UserData("username", "password", "test@email.com");
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.loginUser(newUser);
            });
            Collection<AuthData> auths = getAllAuths();

            assertEquals(401, exception.statusCode());
            assertEquals("Error: unauthorized, no matching user registered", exception.getMessage());
            assertTrue(auths.isEmpty());
        }

        @Test
        @DisplayName("Wrong Password")
        public void testLoginFailureWrongPassword() throws Exception {
            UserData newUser = new UserData("username", "password", "test@email.com");
            AuthData auth = userService.registerUser(newUser);
            userService.logoutUser(auth.authToken());

            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.loginUser(new UserData("username", "wrongpassword", null));
            });
            Collection<AuthData> auths = getAllAuths();

            assertEquals(401, exception.statusCode());
            assertEquals("Error: unauthorized, wrong password", exception.getMessage());
            assertTrue(auths.isEmpty());
        }
    }

    @Test
    @DisplayName("Logout Success")
    public void testLogOutSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);
        assertEquals(getAllAuths().size(), 1);

        userService.logoutUser(auth.authToken());
        assertTrue(getAllAuths().isEmpty());
    }

    @Test
    @DisplayName("Logout Failure - No Authentication")
    public void testLogOutFailureNoAuth() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);

        assertEquals(getAllAuths().size(), 1);
        AuthData badAuth = new AuthData("username", "badauthtoken");
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.logoutUser(badAuth.authToken());
        });

        assertEquals(401, exception.statusCode());
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("Authorize User Success")
    public void testAuthorizeUserSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);
        assertDoesNotThrow(() -> {
            userService.authorizeUser(auth.authToken());
        });
    }

    @Test
    @DisplayName("Authorize User Failure")
    public void testAuthorizeUserFailure() throws Exception {
        String authToken = "randombadauthtoken";
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.authorizeUser(authToken);
        });

        assertEquals(401, exception.statusCode());
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("Clear Users and Auths Success")
    //Part of Clear application so no negative test
    public void testClearUsersAndAuthsSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);

        newUser = new UserData("username1", "password1", "test@email.com");
        auth = userService.registerUser(newUser);

        newUser = new UserData("username2", "password2", "test@email.com");
        auth = userService.registerUser(newUser);

        newUser = new UserData("username3", "password3", "test@email.com");
        auth = userService.registerUser(newUser);

        newUser = new UserData("username4", "password4", "test@email.com");
        auth = userService.registerUser(newUser);

        assertEquals(5, getAllUsers().size());
        assertEquals(5, getAllAuths().size());

        userService.clearUsers();
        userService.clearAuths();

        assertEquals(0, getAllUsers().size());
        assertEquals(0, getAllAuths().size());
    }

    @Test
    @DisplayName("Get User On AuthToken")
    public void testGetUserOnAuthToken() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);

        UserData newUser1 = new UserData("username1", "password1", "test@email.com");
        AuthData auth1 = userService.registerUser(newUser1);

        UserData newUser2 = new UserData("username2", "password2", "test@email.com");
        AuthData auth2 = userService.registerUser(newUser2);

        assertEquals(userService.getUserOnAuthToken(auth.authToken()).username(), newUser.username());
        assertEquals(userService.getUserOnAuthToken(auth.authToken()).email(), newUser.email());
        assertEquals(userService.getUserOnAuthToken(auth1.authToken()).username(), newUser1.username());
        assertEquals(userService.getUserOnAuthToken(auth1.authToken()).email(), newUser1.email());
        assertEquals(userService.getUserOnAuthToken(auth2.authToken()).username(), newUser2.username());
        assertEquals(userService.getUserOnAuthToken(auth2.authToken()).email(), newUser2.email());
    }

    @Test
    @DisplayName("Get User On AuthToken Failure")
    public void testGetUserOnAuthTokenFailure() {
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.getUserOnAuthToken(null);
        });
        assertEquals(500, exception.statusCode());
        assertEquals("Error: AuthToken passed into getUserOnAuthToken is null", exception.getMessage());
    }

    @Test
    @DisplayName("Get User On Username")
    public void testGetUserOnUsername() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);

        UserData newUser1 = new UserData("username1", "password1", "test@email.com");
        AuthData auth1 = userService.registerUser(newUser1);

        UserData newUser2 = new UserData("username2", "password2", "test@email.com");
        AuthData auth2 = userService.registerUser(newUser2);

        assertEquals(userService.getUserOnUserName(auth.username()).username(), newUser.username());
        assertEquals(userService.getUserOnUserName(auth.username()).email(), newUser.email());
        assertEquals(userService.getUserOnUserName(auth1.username()).username(), newUser1.username());
        assertEquals(userService.getUserOnUserName(auth1.username()).email(), newUser1.email());
        assertEquals(userService.getUserOnUserName(auth2.username()).username(), newUser2.username());
        assertEquals(userService.getUserOnUserName(auth2.username()).email(), newUser2.email());
    }

    @Test
    @DisplayName("Get User On Username Failure")
    public void testGetUserOnUsernameFailure() {
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.getUserOnUserName(null);
        });
        assertEquals(500, exception.statusCode());
        assertEquals("Error: Username passed into getUserOnUserName is null", exception.getMessage());
    }

    @Test
    @DisplayName("Get Auth Info On Username")
    public void getAuthInfoOnUsernameSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);
        assertEquals(userService.getAuthInfoByUsername(newUser.username()), auth);
    }

    @Test
    @DisplayName("Get Auth Info On Username Failure")
    public void getAuthInfoOnUsernameFailure() {
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            userService.getAuthInfoByUsername(null);
        });

        assertEquals(500, exception.statusCode());
        assertEquals("Error: Username passed to getAuthInfoByUsername is null", exception.getMessage());
    }


    // ------ Helper functions ------ //
    private Collection<UserData> getAllUsers() throws Exception {
        try {
            return userDataAccess.listUsers();
        } catch (Exception e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    private Collection<AuthData> getAllAuths() throws Exception{
        try {
            return authDataAccess.getAllAuths();
        } catch (Exception e) {
            throw new ServiceException(500, e.getMessage());
        }
    }


}