package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
import org.junit.jupiter.api.*;
import service.GameService;
import service.UserService;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameDataAccessTests {
    private UserService userService;
    private GameService gameService;
    private GameDataAccess gameDataAccess;

    @BeforeEach
    void setup() throws Exception {
        userService = new UserService();
        gameService = new GameService(userService);
        gameDataAccess = gameService.getGameDataAccess();

        gameDataAccess.clearGames();
    }

    @Test
    @DisplayName("Test CreateNewGame Success")
    void testCreateNewGameSuccess() throws Exception {
        gameDataAccess.createNewGame("gameName");

        try {
            var conn = DatabaseManager.getConnection();
            var statement = "SELECT * FROM games WHERE gamename='gameName';";
            var ps = conn.prepareStatement(statement);
            var rs = ps.executeQuery();
            if (rs.next()) {
                GameData game = readTheGame(ps.executeQuery());

                assertEquals("gameName", game.getGameName());
                assertEquals("", game.whiteUsername());
                assertEquals("", game.blackUsername());
                assertEquals(new ChessGame(), game.game());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test CreateNewGame Failure")
    void testCreateNewGameFailure() throws Exception {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDataAccess.createNewGame(null);
        });

        assertEquals(500, exception.statusCode());
        assertEquals("unable to update database: INSERT INTO games (whiteusername, blackusername, " +
                "gamename, game) VALUES (?, ?, ?, ?), Column 'gamename' cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Test GetGameByName Success")
    void testGetGameByNameSuccess() throws Exception {
        gameDataAccess.createNewGame("gameName");

        GameData game = gameDataAccess.getGameByName("gameName");

        assertEquals("gameName", game.getGameName());
        assertEquals("", game.whiteUsername());
        assertEquals("", game.blackUsername());
        assertEquals(new ChessGame(), game.game());
    }

    @Nested
    @DisplayName("Test GetGameByName Failure")
    class GetGameByNameFailure {

        @Test
        @DisplayName("Null Game Name")
        void testGetGameByNameFailureNullName() throws Exception {
            gameDataAccess.createNewGame("gameName");
            gameDataAccess.createNewGame("gameName1");

            GameData game = gameDataAccess.getGameByName(null);

            assertNull(game);
        }

        @Test
        @DisplayName("Bad Game Name")
        void testGetGameByNameFailureBadName() throws Exception {
            gameDataAccess.createNewGame("gameName");
            gameDataAccess.createNewGame("gameName1");

            GameData game = gameDataAccess.getGameByName("badname");

            assertNull(game);
        }
    }

    @Test
    @DisplayName("Test GetGameById Success")
    void testGetGameByIdSuccess() throws Exception {
        gameDataAccess.createNewGame("gameName");
        gameDataAccess.createNewGame("gameName1");

        GameData game = gameDataAccess.getGameById(2);

        assertEquals("gameName1", game.getGameName());
        assertEquals("", game.whiteUsername());
        assertEquals("", game.blackUsername());
        assertEquals(new ChessGame(), game.game());
    }

    @Nested
    @DisplayName("Test GetGameById Failure")
    class GetGameByIdFailure {

        @Test
        @DisplayName("Null Game Id")
        void testGetGameByNameFailureNullName() throws Exception {
            DataAccessException exception = assertThrows(DataAccessException.class, () -> {
                GameData game = gameDataAccess.getGameById(null);
            });

            assertEquals(500, exception.statusCode());
            assertEquals("Cannot invoke \"java.lang.Integer.intValue()\" because \"gameId\" is null", exception.getMessage());
        }

        @Test
        @DisplayName("Bad Game Id")
        void testGetGameByNameFailureBadName() throws Exception {
            gameDataAccess.createNewGame("gameName");
            gameDataAccess.createNewGame("gameName1");

            GameData game = gameDataAccess.getGameById(3);

            assertNull(game);
        }
    }

    @Test
    @DisplayName("Test ClearGames Success")
    void testClearGamesSuccess() throws Exception {
        gameDataAccess.createNewGame("gameName");
        gameDataAccess.createNewGame("gameName1");
        gameDataAccess.createNewGame("gameName2");

        gameDataAccess.clearGames();

        assertEquals(0, gameDataAccess.getAllGames().get("games").size());
    }

    @Test
    @DisplayName("Test GetAllGames Success")
    void testGetAllGamesSuccess() throws Exception {
        gameDataAccess.createNewGame("gameName");
        gameDataAccess.createNewGame("gameName1");
        gameDataAccess.createNewGame("gameName2");

        Map<String, List<Map<String, Object>>> games = gameDataAccess.getAllGames();

        assertEquals(3, games.get("games").size());
        assertEquals("gameName", games.get("games").get(0).get("gameName"));
        assertEquals("gameName1", games.get("games").get(1).get("gameName"));
        assertEquals("gameName2", games.get("games").get(2).get("gameName"));
    }

    @Test
    @DisplayName("Test GetAllGames Failure")
    void testGetAllGamesFailure() throws Exception {
        gameDataAccess = new GameDataAccess() {
            @Override
            public void createNewGame(String gameName) throws Exception {

            }

            @Override
            public void setGameById(Integer gameId, GameData gameData) throws Exception {

            }

            @Override
            public GameData getGameByName(String gameName) throws Exception {
                return null;
            }

            @Override
            public GameData getGameById(Integer gameId) throws Exception {
                return null;
            }

            @Override
            public void clearGames() throws Exception {

            }

            @Override
            public Map<String, List<Map<String, Object>>> getAllGames() throws Exception {
                List<Map<String, Object>> gameList = new ArrayList<>();
                // Made invalid test query
                var statement = "SELECT * FROM non_existent_table";

                try (var conn = DatabaseManager.getConnection()) {
                    try (var ps = conn.prepareStatement(statement)) {
                        try (var rs = ps.executeQuery()) {
                            while (rs.next()) {
                                GameData game = readTheGame(rs);
                                Map<String, Object> gameMap = new HashMap<>();
                                gameMap.put("gameID", game.getGameId());
                                gameMap.put("whiteUsername", game.getWhiteUsername());
                                gameMap.put("blackUsername", game.getBlackUsername());
                                gameMap.put("gameName", game.getGameName());
                                gameList.add(gameMap);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new DataAccessException(500, e.getMessage());
                }
                return Map.of("games", gameList);
            }

            @Override
            public void addUserToGame(String username, JoinGameData gameData) throws Exception {

            }
        };

        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameDataAccess.getAllGames();
        });

        assertTrue(exception.getMessage().contains("non_existent_table"), "Expected SQL syntax error");
    }

    @Test
    @DisplayName("Test AddUserToGame Success")
    void testAddUserToGameSuccess() throws Exception {

        gameDataAccess.createNewGame("gameName");
        gameDataAccess.createNewGame("gameName1");

        gameDataAccess.addUserToGame("testuser", new JoinGameData("WHITE", 1));
        gameDataAccess.addUserToGame("testuser2", new JoinGameData("BLACK", 2));

        Map<String, List<Map<String, Object>>> games = gameDataAccess.getAllGames();

        assertEquals("testuser", games.get("games").get(0).get("whiteUsername"));
        assertEquals("testuser2", games.get("games").get(1).get("blackUsername"));
    }

    @Test
    @DisplayName("Test AddUerToGame Failure")
    void testAddUserToGameFailureBadColor() throws Exception {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDataAccess.addUserToGame("testuser", new JoinGameData("WRONGCOLOR", 1));
        });

        assertEquals(500, exception.statusCode());
        assertEquals("Color passed in is not white or black.", exception.getMessage());
    }


    // ------ HELPER FUNCTIONS ------ //

    // Read game data from statement and pass back in GameData format
    private GameData readTheGame(ResultSet rs) throws Exception {
        int gameId = rs.getInt("gameid");
        String whiteUser = rs.getString("whiteusername");
        String blackUser = rs.getString("blackusername");
        String gameName = rs.getString("gamename");
        String json = rs.getString("game");
        var game = new Gson().fromJson(json, ChessGame.class);
        return new GameData(gameId, whiteUser, blackUser, gameName, game);
    }
}