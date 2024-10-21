package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public Integer getGameId() {
        return gameID;
    }
    public String getWhiteUsername() { return whiteUsername; }
    public String getBlackUsername() { return blackUsername; }
    public String getGameName() { return gameName; }
    public ChessGame getGame() { return game; }
}