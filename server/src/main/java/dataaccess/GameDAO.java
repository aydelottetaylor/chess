package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.HashMap;

public class GameDAO implements GameDataAccess {
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public GameData createNewGame(String gameName) throws Exception {
        int gameId = 1001 + games.size();
        games.put(gameId, new GameData(gameId, null, null, gameName, new ChessGame()));
        return games.get(gameId);
    }

    public GameData getGame(int gameId) {
        return games.get(gameId);
    }

    public void clearGames() {
        games.clear();
    }


}