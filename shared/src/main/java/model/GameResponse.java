package model;

import java.util.List;

public class GameResponse {
    private List<GameData> games;

    // Getter for games
    public List<GameData> getGames() {
        return games;
    }

    public void setGames(List<GameData> games) {
        this.games = games;
    }
}