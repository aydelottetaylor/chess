package ui;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import chess.*;
import com.google.gson.Gson;
import model.*;
import server.ServerFacade;
import ui.websocket.*;
import static ui.EscapeSequences.*;
import static java.util.Objects.isNull;

public class Client {
    private AuthData authData;
    private List<GameData> games;
    StringBuilder gameString;
    private final ServerFacade server;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private State state = State.SIGNED_OUT;

    public Client(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "quit" -> "quit";
                case "creategame" -> createGame(params);
                case "listgames" -> listGames();
                case "playgame" -> playGame(params);
                case "register" -> registerUser(params);
                case "login" -> loginUser(params);
                case "logout" -> logoutUser();
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String playGame(String... params) throws Exception {
        if (state == State.SIGNED_IN) {
            if (isNull(this.games)) {
                throw new ClientException(400, "Make sure to check all games by using 'listGames' first!");
            }

            if (params.length == 2) {
                var number =  Integer.parseInt(params[0]) - 1;
                GameData game = games.get(number);
                server.joinGame(new JoinGameData(params[1].toUpperCase(), game.gameID()), authData.authToken());

                return gameToString(game);
            } else {
                throw new ClientException(400, "Expected: <GameNumber> <Color>");
            }
        } else {
            throwLoggedOut();
            return "";
        }
    }

    public String listGames() throws Exception {
        if (state == State.SIGNED_IN) {
            try {
                fetchGames();

                StringBuilder result = new StringBuilder("Game List:\n");
                for (int i = 0; i < games.size(); i++) {
                    GameData game = games.get(i);
                    result.append(i + 1)
                            .append(": ").append(game.gameName())
                            .append("\n");
                }
                return result.toString();
            } catch (Exception ex) {
                return ex.toString();
            }
        } else {
            throwLoggedOut();
            return "";
        }
    }

    public String createGame(String... params) throws Exception {
        if (state == State.SIGNED_IN) {
            if (params.length == 1) {
                try {
                    String gameName = params[0];
                    server.createGame(new GameData(0, null, null, gameName, null), authData.authToken());
                } catch (Exception ex) {
                    return ex.getMessage();
                }
                return "Game created successfully!";
            }
            throw new ClientException(500, "Expected: <GameName>");
        } else {
            throwLoggedOut();
            return "";
        }
    }

    public String registerUser(String... params) throws Exception {
        if (params.length == 3) {
            UserData user = new UserData(params[0], params[1], params[2]);
            AuthData auth;
            try {
                auth = server.registerUser(user);
            } catch (Exception ex) {
                return ex.getMessage();
            }
            if (auth != null) {
                authData = auth;
                state = State.SIGNED_IN;
                return "Registered and logged in successfully!";
            }
        }
        throw new ClientException(400, "Expected: <Username> <Password> <Email>");
    }

    public String loginUser(String... params) throws Exception {
        if (params.length == 2) {
            UserData user = new UserData(params[0], params[1], null);
            AuthData auth;
            try {
                auth = server.loginUser(user);
            } catch (Exception ex) {
                return ex.getMessage();
            }
            if (auth != null) {
                authData = auth;
                state = State.SIGNED_IN;
                return "Logged in successfully!";
            }
        }
        throw new ClientException(400, "Expected: <Username> <Password>");
    }

    public String logoutUser() throws Exception {
        if (state == State.SIGNED_IN) {
            try {
                server.logoutUser(authData.authToken());
                state = State.SIGNED_OUT;
                return "Logged out successfully!";
            } catch (Exception ex) {
                throw new ClientException(500, ex.getMessage());
            }
        }
        throwLoggedOut();
        return "";
    }

    private void fetchGames() throws Exception {
        games = server.fetchAllGames(authData.authToken());
    }

    private void throwLoggedOut() throws Exception {
        throw new ClientException(400, "Logged out, cannot perform command.");
    }

    private String gameToString(GameData gameData) throws Exception {
        gameString = new StringBuilder();
        ChessGame game = gameData.game();
        ChessBoard board = game.getBoard();
        buildBoard(board);
        buildBackwardsBoard(board);


        gameString.append(SET_BG_COLOR_BLACK
                + "                                   "
                + RESET_BG_COLOR
                + "\n");
        String theBoard = gameString.toString();

        return theBoard;
    }

    private void buildBoard(ChessBoard board) {
        for (int i = 0; i < 10; i++) {
            if (i == 0 || i == 9) {
                gameString.append(SET_BG_COLOR_DARK_GREY
                        +  SET_TEXT_COLOR_WHITE
                        + "    h   g   f  e   d   c  b   a    "
                        + RESET_BG_COLOR + "\n");
            } else if (i % 2 != 0) {
                for (int j = 0; j < 10; j++) {
                    if (j == 0 || j == 9) {
                        gameString.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " ").
                                append(i).append(" ").append(RESET_BG_COLOR);
                    } else if (j % 2 != 0) {
                        gameString.append(SET_BG_COLOR_LIGHT_GREY);
                        addPiece(board, i, j);
                    } else {
                        gameString.append(SET_BG_COLOR_BLACK);
                        addPiece(board, i, j);
                    }
                }
                gameString.append("\n");
            } else {
                for (int j = 0; j < 10; j++) {
                    if (j == 0 || j == 9) {
                        gameString.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " ").
                                append(i).append(" ").append(RESET_BG_COLOR);
                    } else if (j % 2 != 0) {
                        gameString.append(SET_BG_COLOR_BLACK);
                        addPiece(board, i, j);
                    } else {
                        gameString.append(SET_BG_COLOR_LIGHT_GREY);
                        addPiece(board, i, j);
                    }
                }
                gameString.append("\n");
            }
        }
    }

    public void buildBackwardsBoard(ChessBoard board) {
        
    }

    private void addPiece(ChessBoard board, int i, int j) {
        if (board.getPiece(new ChessPosition(i, j)) == null) {
            gameString.append(EMPTY);
        } else {
            ChessPiece piece = board.getPiece(new ChessPosition(i, j));
            switch(piece.type) {
                case KING -> {
                    switch (piece.pieceColor) {
                        case WHITE -> gameString.append(WHITE_KING);
                        case BLACK -> gameString.append(BLACK_KING);
                    }
                }
                case QUEEN -> {
                    switch (piece.pieceColor) {
                        case WHITE -> gameString.append(WHITE_QUEEN);
                        case BLACK -> gameString.append(BLACK_QUEEN);
                    }
                }
                case ROOK -> {
                    switch (piece.pieceColor) {
                        case WHITE -> gameString.append(WHITE_ROOK);
                        case BLACK -> gameString.append(BLACK_ROOK);
                    }
                }
                case BISHOP -> {
                    switch (piece.pieceColor) {
                        case WHITE -> gameString.append(WHITE_BISHOP);
                        case BLACK -> gameString.append(BLACK_BISHOP);
                    }
                }
                case KNIGHT -> {
                    switch (piece.pieceColor) {
                        case WHITE -> gameString.append(WHITE_KNIGHT);
                        case BLACK -> gameString.append(BLACK_KNIGHT);
                    }
                }
                case PAWN -> {
                    switch (piece.pieceColor) {
                        case WHITE -> gameString.append(WHITE_PAWN);
                        case BLACK -> gameString.append(BLACK_PAWN);
                    }
                }
            }
        }
    }

    public String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    - register <Username> <Password> <Email> - create an account
                    - login <Username> <Password> - to play chess
                    - quit - to exit
                    - help - get possible commands
                    """;
        }
        return """
                - logout
                - createGame <GameName> - to create a game
                - listGames - to list all games
                - playGame <GameNumber> <Color> - to play a game
                - observeGame <GameNumber> - to watch a game
                - quit - to exit
                - help - get possible commands
                """;
    }



    public String getState() {
        return state.toString();
    }
}