package dataaccess;

import model.GameData;
import model.JoinGameData;

import java.util.List;
import java.util.Map;

public interface GameDataAccess {
    GameData createNewGame(String gameName) throws Exception;
    void clearGames() throws Exception;
//    Map<String, List<Map<String, Object>>> getAllGames();
    void addUserToGame(String username, JoinGameData gameData) throws Exception;

}

