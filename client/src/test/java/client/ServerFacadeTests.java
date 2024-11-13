package client;

import model.*;
import org.junit.jupiter.api.*;
import server.*;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port + "/";

        serverFacade = new ServerFacade(url);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        serverFacade.clear();
    }

    @Test
    @DisplayName("Test Register - Positive")
    public void testRegisterPositive() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 19);
        assertEquals(user.username(), auth.username());
    }

    @Test
    @DisplayName("Test Register - Negative")
    public void testRegisterNegative() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        ServerFacadeException exception = assertThrows(ServerFacadeException.class, () -> {
            serverFacade.registerUser(user);
        });

        assertEquals(exception.getMessage(), "Error: Username already taken");
        assertEquals(500, exception.statusCode());
    }

    @Test
    @DisplayName("Test Login - Positive")
    public void testLoginPositive() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);
        assertNotNull(auth.authToken());

        AuthData newAuth = serverFacade.loginUser(user);
        assertNotNull(newAuth.authToken());
        assertTrue(newAuth.authToken().length() > 19);
        assertEquals(newAuth.username(), auth.username());
    }

    @Test
    @DisplayName("Test Login - Negative")
    public void testLoginNegative() throws Exception {
        ServerFacadeException exception = assertThrows(ServerFacadeException.class, () -> {
            serverFacade.loginUser(new UserData("nonexistentuser", "password", null));
        });

        assertEquals(exception.getMessage(), "Error: Unauthorized, no matching user registered");
        assertEquals(500, exception.statusCode());
    }

    @Test
    @DisplayName("Test Logout - Positive")
    public void testLogoutPositive() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);
        assertNotNull(auth.authToken());

        assertDoesNotThrow(() -> serverFacade.logoutUser(auth.authToken()));
    }

    @Test
    @DisplayName("Test Logout - Negative")
    public void testLogoutNegative() throws Exception {
        ServerFacadeException exception = assertThrows(ServerFacadeException.class, () -> {
            serverFacade.logoutUser("authtokenthatdoesntexist");
        });

        assertEquals(500, exception.statusCode());
        assertEquals("Error: Unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("CreateGame - Positive")
    public void testCreateGame() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        GameData game = new GameData(0, null, null, "gameName", null);
        Object response = serverFacade.createGame(game, auth.authToken());
        assertNotNull(response);
    }

    @Test
    @DisplayName("Create Game - Negative")
    public void testCreateGameNegative() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        GameData game = new GameData(0, null, null, "gameName", null);
        serverFacade.createGame(game, auth.authToken());

        ServerFacadeException exception = assertThrows(ServerFacadeException.class, () -> {
            serverFacade.createGame(game, auth.authToken());
        });

        assertEquals(exception.getMessage(), "Game name already taken.");
        assertEquals(500, exception.statusCode());
    }

    @Test
    @DisplayName("Fetch All Games - Positive")
    public void testFetchAllGames() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        serverFacade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());
        serverFacade.createGame(new GameData(0, null, null, "gameName1", null), auth.authToken());
        serverFacade.createGame(new GameData(0, null, null, "gameName2", null), auth.authToken());

        List<GameData> games = serverFacade.fetchAllGames(auth.authToken());

        assertTrue(games.size() == 3);
        assertTrue(Objects.equals(games.getFirst().gameName(), "gameName"));
        assertTrue(Objects.equals(games.getLast().gameName(), "gameName2"));
    }

    @Test
    @DisplayName("Fetch All Games - Negative")
    public void testFetchAllGamesNegative() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        serverFacade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());
        serverFacade.createGame(new GameData(0, null, null, "gameName1", null), auth.authToken());
        serverFacade.createGame(new GameData(0, null, null, "gameName2", null), auth.authToken());

        ServerFacadeException exception = assertThrows(ServerFacadeException.class, () -> {
            serverFacade.fetchAllGames("fakeauthtoken");
        });

        assertEquals(500, exception.statusCode());
        assertEquals("Error: Unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("Join Game - Positive")
    public void testJoinGamePositive() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        serverFacade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());
        serverFacade.joinGame(new JoinGameData("WHITE", 1), auth.authToken());

        List<GameData> games = serverFacade.fetchAllGames(auth.authToken());
        GameData game = games.get(0);
        assertEquals(game.gameName(), "gameName");
        assertEquals(game.whiteUsername(), "username");
    }

    @Test
    @DisplayName("Join Game - Negative")
    public void testJoinGameNegative() throws Exception {
        UserData user = new UserData("username", "password", "email");
        AuthData auth = serverFacade.registerUser(user);

        serverFacade.createGame(new GameData(0, null, null, "gameName", null), auth.authToken());

        ServerFacadeException exception = assertThrows(ServerFacadeException.class, () -> {
            serverFacade.joinGame(new JoinGameData("WHIT", 1), auth.authToken());
        });

        assertEquals(500, exception.statusCode());
        assertEquals("Error: Color must be 'WHITE' or 'BLACK'", exception.getMessage());
    }


}
