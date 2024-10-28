package service;

import chess.ChessGame;
import model.*;
import service.*;
import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GameServiceTests {
    private UserService userService;
    private GameService gameService;
    private GameDataAccess gameDataAccess;
    private UserDataAccess userDataAccess;
    private AuthDataAccess authDataAccess;

    @BeforeEach
    void setup() {
        userService = new UserService();
        gameService = new GameService(userService);
        gameDataAccess = gameService.getGameDataAccess();
        userDataAccess = userService.getUserDataAccess();
        authDataAccess = userService.getAuthDataAccess();
    }

    @Test
    @DisplayName("Create Game Success")
    public void createGameSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "email@email.com");
        AuthData auth = userService.registerUser(newUser);
        GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());

        GameData game = gameService.createGame(newGame, auth.authToken());
        assertNotNull(game);

        Map<String, List<Map<String, Object>>> allGames = gameDataAccess.getAllGames();
        Map<String, Object> gameMap = allGames.get("games").getFirst();

        assertEquals(1, allGames.get("games").size());
        assertEquals(1001, gameMap.get("gameID"));
        assertEquals("newGame", gameMap.get("gameName"));
        assertNull(gameMap.get("whiteUsername"));
        assertNull(gameMap.get("blackUsername"));
        assertNotNull(gameMap.get("gameID"));

        assertEquals(game.gameName(), gameMap.get("gameName"));
        assertEquals(game.gameID(), gameMap.get("gameID"));
        assertEquals(game.whiteUsername(), gameMap.get("whiteUsername"));
        assertEquals(game.blackUsername(), gameMap.get("blackUsername"));

    }

    @Nested
    @DisplayName("Create Game Failures")
    class TestCreateGameFailure {

        @Test
        @DisplayName("Not Authorized")
        public void testCreateGameFailureNotAuthorized() {
            GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());
            ServiceException exception = assertThrows(ServiceException.class, () -> {
               gameService.createGame(newGame, null);
            });
            assertEquals(401, exception.statusCode());
            assertEquals("Error: unauthorized", exception.getMessage());
        }

        @Test
        @DisplayName("Game Name Is Null")
        public void testCreateGameFailureNullGameName() throws Exception {
            GameData newGame = new GameData(0, null, null, null, new ChessGame());
            UserData newUser = new UserData("username", "password", "email@email.com");
            AuthData auth = userService.registerUser(newUser);

            ServiceException exception = assertThrows(ServiceException.class, () -> {
                gameService.createGame(newGame, auth.authToken());
            });
            assertEquals(500, exception.statusCode());
            assertEquals("Error: game name is null or empty, must give a game name", exception.getMessage());
        }

        @Test
        @DisplayName("Game Name Is Empty")
        public void testCreateGameFailureEmptyGameName() throws Exception {
            GameData newGame = new GameData(0, null, null, "", new ChessGame());
            UserData newUser = new UserData("username", "password", "email@email.com");
            AuthData auth = userService.registerUser(newUser);

            ServiceException exception = assertThrows(ServiceException.class, () -> {
                gameService.createGame(newGame, auth.authToken());
            });
            assertEquals(500, exception.statusCode());
            assertEquals("Error: game name is null or empty, must give a game name", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Clear Database Success")
    // Is Clear application so no negative test
    public void testClearDataBase() throws Exception {
        UserData newUser = new UserData("username", "password", "email@email.com");
        userService.registerUser(newUser);
        newUser = new UserData("username2", "password2", "email@email.com");
        userService.registerUser(newUser);
        newUser = new UserData("username3", "password3", "email@email.com");
        userService.registerUser(newUser);
        newUser = new UserData("username4", "password4", "email@email.com");
        userService.registerUser(newUser);
        newUser = new UserData("username5", "password5", "email@email.com");
        AuthData auth = userService.registerUser(newUser);

        assertEquals(5, getAllUsers().size());
        assertEquals(5, getAllAuths().size());

        GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());
        for (int i = 0; i < 5; ++i) {
            gameService.createGame(newGame, auth.authToken());
        }
        assertEquals(5, getAllGames().get("games").size());

        gameService.clearDatabase();

        assertEquals(0, getAllUsers().size());
        assertEquals(0, getAllAuths().size());
        assertEquals(0, getAllGames().get("games").size());
    }

    @Test
    @DisplayName("Get All Games Success")
    public void testGetAllGames() throws Exception {
        UserData newUser = new UserData("username", "password", "email@email.com");
        AuthData auth = userService.registerUser(newUser);

        setupGames(auth);

        List<Map<String, Object>> games = gameService.getAllGames(auth.authToken()).get("games");

        assertEquals(5, games.size());

        assertEquals(1001, games.getFirst().get("gameID"));
        assertEquals("newGame", games.getFirst().get("gameName"));
        assertNull(games.get(0).get("blackUsername"));
        assertEquals("username", games.getFirst().get("whiteUsername"));

        assertEquals(1002, games.get(1).get("gameID"));
        assertEquals("newGame1", games.get(1).get("gameName"));
        assertNull(games.get(1).get("whiteUsername"));
        assertEquals("username", games.get(1).get("blackUsername"));

        assertEquals(1003, games.get(2).get("gameID"));
        assertEquals("newGame2", games.get(2).get("gameName"));
        assertNull(games.get(2).get("blackUsername"));
        assertEquals("username", games.get(2).get("whiteUsername"));

        assertEquals(1004, games.get(3).get("gameID"));
        assertEquals("newGame3", games.get(3).get("gameName"));
        assertNull(games.get(3).get("whiteUsername"));
        assertEquals("username", games.get(3).get("blackUsername"));

        assertEquals(1005, games.get(4).get("gameID"));
        assertEquals("newGame4", games.get(4).get("gameName"));
        assertNull(games.get(4).get("whiteUsername"));
        assertEquals("username", games.get(4).get("blackUsername"));
    }

    @Test
    @DisplayName("Get All Games Failure")
    public void testGetAllGamesFailure() throws Exception {
        UserData newUser = new UserData("username", "password", "email@email.com");
        AuthData auth = userService.registerUser(newUser);
        GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());
        gameService.createGame(newGame, auth.authToken());

        assertEquals(1, getAllGames().get("games").size());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            gameService.getAllGames("fakeauthtoken");
        });

        assertEquals(401, exception.statusCode());
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @DisplayName("Join Game Success")
    public void testJoinGameSuccess() throws Exception {
        UserData newUser = new UserData("username", "password", "email@email.com");
        AuthData auth = userService.registerUser(newUser);
        newUser = new UserData("username2", "password", "email@email.com");
        AuthData auth2 = userService.registerUser(newUser);

        setupGames(auth);
        gameService.joinGame(auth2.authToken(), new JoinGameData("BLACK", 1001));
        gameService.joinGame(auth2.authToken(), new JoinGameData("WHITE", 1004));
        gameService.joinGame(auth2.authToken(), new JoinGameData("WHITE", 1005));

        List<Map<String, Object>> games = gameService.getAllGames(auth.authToken()).get("games");

        assertEquals(5, games.size());

        assertEquals(1001, games.getFirst().get("gameID"));
        assertEquals("newGame", games.getFirst().get("gameName"));
        assertEquals("username2", games.get(0).get("blackUsername"));
        assertEquals("username", games.getFirst().get("whiteUsername"));

        assertEquals(1002, games.get(1).get("gameID"));
        assertEquals("newGame1", games.get(1).get("gameName"));
        assertNull(games.get(1).get("whiteUsername"));
        assertEquals("username", games.get(1).get("blackUsername"));

        assertEquals(1003, games.get(2).get("gameID"));
        assertEquals("newGame2", games.get(2).get("gameName"));
        assertNull(games.get(2).get("blackUsername"));
        assertEquals("username", games.get(2).get("whiteUsername"));

        assertEquals(1004, games.get(3).get("gameID"));
        assertEquals("newGame3", games.get(3).get("gameName"));
        assertEquals("username2", games.get(3).get("whiteUsername"));
        assertEquals("username", games.get(3).get("blackUsername"));

        assertEquals(1005, games.get(4).get("gameID"));
        assertEquals("newGame4", games.get(4).get("gameName"));
        assertEquals("username2", games.get(4).get("whiteUsername"));
        assertEquals("username", games.get(4).get("blackUsername"));
    }

    @Nested
    @DisplayName("Join Game Failure")
    class JoinGameFailure {

        @Test
        @DisplayName("Bad Authorization")
        public void joinGameFailureBadAuthorization() throws Exception {
            UserData newUser = new UserData("username", "password", "email@email.com");
            AuthData auth = userService.registerUser(newUser);
            GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());
            gameService.createGame(newGame, auth.authToken());

            ServiceException exception = assertThrows(ServiceException.class, () -> {
                gameService.joinGame("badauthtoken", new JoinGameData("WHITE", 1001));
            });

            assertEquals(401, exception.statusCode());
            assertEquals("Error: unauthorized", exception.getMessage());
        }

        @Test
        @DisplayName("Bad Request - Color Not White or Black")
        public void joinGameFailureBadRequest() throws Exception {
            UserData newUser = new UserData("username", "password", "email@email.com");
            AuthData auth = userService.registerUser(newUser);
            GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());
            gameService.createGame(newGame, auth.authToken());

            ServiceException exception = assertThrows(ServiceException.class, () -> {
                gameService.joinGame(auth.authToken(), new JoinGameData("RANDOMCOLOR", 1001));
            });

            assertEquals(400, exception.statusCode());
            assertEquals("Error: bad request", exception.getMessage());
        }

        @Test
        @DisplayName("Bad Request - No Matching Game Found")
        public void joinGameFailureBadRequestNoMatchingGame() throws Exception {
            UserData newUser = new UserData("username", "password", "email@email.com");
            AuthData auth = userService.registerUser(newUser);

            DataAccessException exception = assertThrows(DataAccessException.class, () -> {
                gameService.joinGame(auth.authToken(), new JoinGameData("WHITE", 1001));
            });

            assertEquals(400, exception.statusCode());
            assertEquals("Error: bad request", exception.getMessage());
        }

        @Test
        @DisplayName("Color Already Taken")
        public void joinGameFailureColorAlreadyTaken() throws Exception {
            UserData newUser = new UserData("username", "password", "email@email.com");
            AuthData auth = userService.registerUser(newUser);
            GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());
            gameService.createGame(newGame, auth.authToken());
            gameService.joinGame(auth.authToken(), new JoinGameData("WHITE", 1001));

            newUser = new UserData("username2", "password", "email@email.com");
            AuthData auth2 = userService.registerUser(newUser);

            DataAccessException exception = assertThrows(DataAccessException.class, () -> {
                gameService.joinGame(auth2.authToken(), new JoinGameData("WHITE", 1001));
            });

            assertEquals(403, exception.statusCode());
            assertEquals("Error: already taken", exception.getMessage());
        }

    }

    // ------ Helper functions ------ //
    private Map<String, List<Map<String, Object>>> getAllGames() throws Exception{
        return gameDataAccess.getAllGames();
    }

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

    private void setupGames(AuthData auth) throws Exception {
        GameData newGame = new GameData(0, null, null, "newGame", new ChessGame());
        gameService.createGame(newGame, auth.authToken());

        newGame = new GameData(0, null, null, "newGame1", new ChessGame());
        gameService.createGame(newGame, auth.authToken());

        newGame = new GameData(0, null, null, "newGame2", new ChessGame());
        gameService.createGame(newGame, auth.authToken());

        newGame = new GameData(0, null, null, "newGame3", new ChessGame());
        gameService.createGame(newGame, auth.authToken());

        newGame = new GameData(0, null, null, "newGame4", new ChessGame());
        gameService.createGame(newGame, auth.authToken());

        gameService.joinGame(auth.authToken(), new JoinGameData("WHITE", 1001));
        gameService.joinGame(auth.authToken(), new JoinGameData("BLACK", 1002));
        gameService.joinGame(auth.authToken(), new JoinGameData("WHITE", 1003));
        gameService.joinGame(auth.authToken(), new JoinGameData("BLACK", 1004));
        gameService.joinGame(auth.authToken(), new JoinGameData("BLACK", 1005));
    }

}
