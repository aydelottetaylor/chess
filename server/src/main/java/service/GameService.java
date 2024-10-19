package service;

import dataaccess.*;
import model.*;

import java.security.Provider;
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

    public GameData createGame(GameData gameInfo, String authToken) throws Exception {
        userService.authorizeUser(authToken);
        return gameDataAccess.createNewGame(gameInfo.gameName());
    }

    public void clearDatabase() throws Exception {
        userService.clearUsersAndAuths();
        gameDataAccess.clearGames();
    }

    public Map<String, List<Map<String, Object>>> getAllGames(String authToken) throws Exception {
        userService.authorizeUser(authToken);
        return gameDataAccess.getAllGames();
    }

    public void joinGame(String authToken, JoinGameData gameData) throws Exception {
        userService.authorizeUser(authToken);
        if (!Objects.equals(gameData.playerColor(), "WHITE") && !Objects.equals(gameData.playerColor(), "BLACK")) {
            throw new ServiceException(400, "Error: bad request");
        }
        UserData user = userService.getUserOnAuthToken(authToken);
        gameDataAccess.addUserToGame(user.username(), gameData);
    }
}