package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.JoinGameData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameDAO implements GameDataAccess {
    private final HashMap<Integer, GameData> games = new HashMap<>();

    // Takes a gameName and generates new game data, stores in stored data and then returns game data
    public GameData createNewGame(String gameName) throws Exception {
        int gameId = 1001 + games.size();
        games.put(gameId, new GameData(gameId, null, null, gameName, new ChessGame()));
        return games.get(gameId);
    }

    // Clears all game data from stored data
    public void clearGames() {
        games.clear();
    }

    // Returns all game data 
    public Map<String, List<Map<String, Object>>> getAllGames() {
        List<Map<String, Object>> gameList = games.values().stream().map( game -> {
            Map<String, Object> gameMap = new HashMap<>();
            gameMap.put("gameID", game.gameID());
            gameMap.put("whiteUsername", game.whiteUsername() != null ? game.whiteUsername() : null);
            gameMap.put("blackUsername", game.blackUsername() != null ? game.blackUsername() : null);
            gameMap.put("gameName", game.gameName());
            return gameMap;
        }).toList();

        return Map.of("games", gameList);
    }

    // Takes a username and JoinGameData, checks to see if color has been taken for specific game, if it hasn't adds user to game as color
    public void addUserToGame(String username, JoinGameData gameData) throws Exception {
        GameData originalGame = games.get(gameData.gameID());
        if (originalGame != null) {
            if(Objects.equals(gameData.playerColor(), "WHITE") && originalGame.whiteUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            } else if (Objects.equals(gameData.playerColor(), "BLACK") && originalGame.blackUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            }
            games.remove(gameData.gameID());
            games.put(gameData.gameID(), new GameData(originalGame.gameID(),
                    Objects.equals(gameData.playerColor(), "WHITE") ? username : originalGame.whiteUsername(),
                    Objects.equals(gameData.playerColor(), "BLACK") ? username : originalGame.blackUsername(),
                    originalGame.gameName(),
                    originalGame.game()));
        } else {
            throw new DataAccessException(400, "Error: bad request");
        }

    }

}