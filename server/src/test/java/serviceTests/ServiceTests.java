package serviceTests;

import chess.ChessGame;
import dataaccess.GameDataAccess;
import model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import service.*;
import dataaccess.*;

import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;


public class ServiceTests {
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setup() {
        userService = new UserService();
        gameService = new GameService(userService);
    }

    @Test
    @DisplayName("RegisterUser - Success")
    void testRegisterUser() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData response = userService.registerUser(newUser);
        UserData checkUser = userService.getUserOnUserName(newUser.username());
        AuthData checkAuth = userService.getAuthInfoByUsername(newUser.username());

        assertEquals(newUser.getUsername(), response.getUsername(),
                "Test RegisterUser response did not return same username as given user.");
        assertNotNull(response.authToken(),
                "Test RegisterUser response did not return an authorization token.");

        assertEquals(newUser, checkUser);
        assertNotNull(checkAuth);
    }

    @Nested
    @DisplayName("RegisterUser - Bad Request")
    class testRegisterUserBadRequest {

        @Test
        @DisplayName("No Username")
        public void testRegisterUserBadRequestBadUsername() {
            UserData newUser = new UserData(null, "password", "test@email.com");
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            Collection<UserData> users = userService.getAllUsers();
            Collection<AuthData> auths = userService.getAllAuths();

            assertEquals(400, exception.StatusCode());
            assertEquals("Error: bad request", exception.getMessage());
            assertTrue(users.isEmpty());
            assertTrue(auths.isEmpty());
        }

        @Test
        @DisplayName("No Password")
        public void testRegisterUserBadRequestBadPassword() {
            UserData newUser = new UserData("username", null, "test@email.com");
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            Collection<UserData> users = userService.getAllUsers();
            Collection<AuthData> auths = userService.getAllAuths();

            assertEquals(400, exception.StatusCode());
            assertEquals("Error: bad request", exception.getMessage());
            assertTrue(users.isEmpty());
            assertTrue(auths.isEmpty());
        }

        @Test
        @DisplayName("No Email")
        public void testRegisterUserBadRequestBadEmail() {
            UserData newUser = new UserData("username", "password", null);
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            Collection<UserData> users = userService.getAllUsers();
            Collection<AuthData> auths = userService.getAllAuths();

            assertEquals(400, exception.StatusCode());
            assertEquals("Error: bad request", exception.getMessage());
            assertTrue(users.isEmpty());
            assertTrue(auths.isEmpty());
        }
    }

    @Test
    @DisplayName("LoginUser - Success")
    public void testLoginSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "test@email.com");
        AuthData auth = userService.registerUser(newUser);
        AuthData authorization = userService.loginUser(newUser);
        Collection<AuthData> auths = userService.getAllAuths();
        UserData user = userService.getUserOnUserName(newUser.username());

        assertNotNull(authorization);
        assertEquals(auths.size(), 1);
        assertEquals(user, newUser);

        newUser = new UserData("username1", "password", "test@email.com");
        auth = userService.registerUser(newUser);
        authorization = userService.loginUser(newUser);
        auths = userService.getAllAuths();
        user = userService.getUserOnUserName(newUser.username());

        assertNotNull(authorization);
        assertEquals(auths.size(), 2);
        assertEquals(user, newUser);
    }

    @Nested
    @DisplayName("LoginUser - Failure")
    class testLoginFailure {

        @Test
        @DisplayName("No Matching User Info")
        public void testLoginFailureNoUser() {
            UserData newUser = new UserData("username", "password", "test@email.com");
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.loginUser(newUser);
            });
            Collection<AuthData> auths = userService.getAllAuths();

            assertEquals(401, exception.StatusCode());
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
            Collection<AuthData> auths = userService.getAllAuths();

            assertEquals(401, exception.StatusCode());
            assertEquals("Error: unauthorized, wrong password", exception.getMessage());
            assertTrue(auths.isEmpty());
        }


    }





}