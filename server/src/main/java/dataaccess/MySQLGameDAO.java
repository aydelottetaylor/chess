package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;

import java.sql.*;
import java.util.*;

public class MySQLGameDAO implements GameDataAccess {

    // Creates a new game and adds to database
    public void createNewGame(String gameName) throws Exception {
        try {
            var statement = "INSERT INTO games (whiteusername, blackusername, gamename, game) VALUES (?, ?, ?, ?)";
            var json = new Gson().toJson(new ChessGame());
            DatabaseManager.executeUpdate(statement, "", "", gameName, json);
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    // Get game by game name
    public GameData getGameByName(String gameName) throws Exception {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gamename=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, gameName);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
        return null;
    }

    // Get game by game id
    public GameData getGameById(Integer gameId) throws Exception {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gameid=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameId);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
        return null;
    }

    // Read game data from statement and pass back in GameData format
    private GameData readGame(ResultSet rs) throws Exception {
        int gameId = rs.getInt("gameid");
        var whiteUser = rs.getString("whiteusername");
        var blackUser = rs.getString("blackusername");
        var gameName = rs.getString("gamename");
        var json = rs.getString("game");
        var game = new Gson().fromJson(json, ChessGame.class);
        return new GameData(gameId, whiteUser, blackUser, gameName, game);
    }

    // Clear all games from database
    public void clearGames() throws Exception {
        try {
            var statement = "TRUNCATE TABLE games";
            DatabaseManager.executeUpdate(statement);
        } catch(Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    // Get all games in database
    public Map<String, List<Map<String, Object>>> getAllGames() throws Exception {
        List<Map<String, Object>> gameList = new ArrayList<>();
        var statement = "SELECT * FROM games";

        try (Connection conn = DatabaseManager.getConnection()) {
             try(var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GameData game = readGame(rs);

                        Map<String, Object> gameMap = new HashMap<>();
                        gameMap.put("gameID", game.getGameId());
                        gameMap.put("whiteUsername", !Objects.equals(game.getWhiteUsername(), "") ? game.getWhiteUsername() : null);
                        gameMap.put("blackUsername", !Objects.equals(game.getBlackUsername(), "") ? game.getBlackUsername() : null);
                        gameMap.put("gameName", game.getGameName());
                        gameMap.put("game", game.getGame());

                        gameList.add(gameMap);
                    }
                }
             }
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
        return Map.of("games", gameList);
    }

    public void setGameById(Integer gameId, GameData game) throws Exception {
        try {
            var statement = "UPDATE games SET game = ? WHERE gameid = ?";
            var json = new Gson().toJson(game.getGame());
            DatabaseManager.executeUpdate(statement, json, gameId);
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    // Add user to specified game by gameId
    public void addUserToGame(String username, JoinGameData gameData) throws Exception {
        try {
            if (Objects.equals(gameData.playerColor(), "WHITE")) {
                var statement = "UPDATE games SET whiteusername=? WHERE gameid=?";
                DatabaseManager.executeUpdate(statement, username, gameData.gameID());
            } else if (Objects.equals(gameData.playerColor(), "BLACK")){
                var statement = "UPDATE games SET blackusername=? WHERE gameid=?";
                DatabaseManager.executeUpdate(statement, username, gameData.gameID());
            } else {
                throw new DataAccessException(500, "Color passed in is not white or black.");
            }
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }


}