package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MySQL_GameDAO implements GameDataAccess {

    public GameData createNewGame(String gameName) throws Exception {
        return new GameData(1, "", "", "", new ChessGame());
    }

    public void clearGames() throws Exception {
        try {
            var statement = "TRUNCATE TABLE games";
            DatabaseManager.executeUpdate(statement);
        } catch(Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

//    public Map<String, List<Map<String, Object>>> getAllGames() {
//
//    }

    public void addUserToGame(String username, JoinGameData gameData) {

    }


}