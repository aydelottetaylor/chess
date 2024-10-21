package model;

public record JoinGameData(String playerColor, Integer gameID) {
    public String getPlayerColor() {return playerColor;}
    public Integer getGameID() {return gameID;}
}