package dataaccess;

import model.GameData;

public interface GameDataAccess {
    GameData createNewGame(String gameName) throws Exception;
    GameData getGame(int gameId);

}

