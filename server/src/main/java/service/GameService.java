package service;

import dataaccess.*;
import model.*;

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
}