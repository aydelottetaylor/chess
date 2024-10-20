package serviceTests;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import service.*;

import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
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

        Assertions.assertEquals(newUser.getUsername(), response.getUsername(),
                "Test RegisterUser response did not return same username as given user.");
        Assertions.assertNotNull(response.authToken(),
                "Test RegisterUser response did not return an authorization token.");
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
            assertEquals(400, exception.StatusCode());
            assertEquals("Error: bad request", exception.getMessage());
        }

        @Test
        @DisplayName("No Password")
        public void testRegisterUserBadRequestBadPassword() {
            UserData newUser = new UserData("username", null, "test@email.com");
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            assertEquals(400, exception.StatusCode());
            assertEquals("Error: bad request", exception.getMessage());
        }

        @Test
        @DisplayName("No Email")
        public void testRegisterUserBadRequestBadEmail() {
            UserData newUser = new UserData("username", "password", null);
            ServiceException exception = assertThrows(ServiceException.class, () -> {
                userService.registerUser(newUser);
            });
            assertEquals(400, exception.StatusCode());
            assertEquals("Error: bad request", exception.getMessage());
        }
    }





}