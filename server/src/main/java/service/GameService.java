package service;

import dataaccess.*;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameService {
    private final GameDataAccess gameDataAccess;
    private final UserService userService;

    public GameService(UserService userService) {
        this.gameDataAccess = new GameDAO();
        this.userService = userService;
    }

    // Create a new game using given game information, checks user authorization
    public GameData createGame(GameData gameInfo, String authToken) throws Exception {
        if(gameInfo.gameName() == null || gameInfo.gameName().isEmpty()) {
            throw new ServiceException(500, "Error: game name is null or empty, must give a game name");
        }
        userService.authorizeUser(authToken);
        return gameDataAccess.createNewGame(gameInfo.gameName());
    }

    // Calls clear users and auths in UserService and then clears game data
    public void clearDatabase() throws Exception {
        try {
            userService.clearUsersAndAuths();
            gameDataAccess.clearGames();
        } catch (Exception e) {
            throw new ServiceException(500, "Error: error thrown in clear database");
        }
    }

    // Returns all existing games, checks user authorization
    public Map<String, List<Map<String, Object>>> getAllGames(String authToken) throws Exception {
        userService.authorizeUser(authToken);
        return gameDataAccess.getAllGames();
    }

    // Adds user to game as requested color, checks user authorization and that the color is WHITE or BLACK
    public void joinGame(String authToken, JoinGameData gameData) throws Exception {
        userService.authorizeUser(authToken);
        if (!Objects.equals(gameData.playerColor(), "WHITE") && !Objects.equals(gameData.playerColor(), "BLACK")) {
            throw new ServiceException(400, "Error: bad request");
        }
        UserData user = userService.getUserOnAuthToken(authToken);
        gameDataAccess.addUserToGame(user.username(), gameData);
    }

    // ------ HELPER FUNCTIONS FOR SERVICE TESTS ------ //
    public GameDataAccess getGameDataAccess() {
        return gameDataAccess;
    }

}