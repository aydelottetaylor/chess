package dataaccess;

import model.GameData;
import model.JoinGameData;

import java.util.List;
import java.util.Map;

public interface GameDataAccess {
    void createNewGame(String gameName) throws Exception;
    GameData getGameByName(String gameName) throws Exception;
    GameData getGameById(Integer gameId) throws Exception;
    void setGameById(Integer gameId, GameData gameData) throws Exception;
    void clearGames() throws Exception;
    Map<String, List<Map<String, Object>>> getAllGames() throws Exception;
    void addUserToGame(String username, JoinGameData gameData) throws Exception;

}

