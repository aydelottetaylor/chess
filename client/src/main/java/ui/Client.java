package ui;

import java.util.*;

import chess.*;
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
    public GameData currentGame;
    private String currentColor;
    private Integer currentGameNumber;
    private static ArrayList<ChessPosition> positions = new ArrayList<>();


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
                case "observegame" -> observeGame(params);
                case "register" -> registerUser(params);
                case "login" -> loginUser(params);
                case "logout" -> logoutUser();
                case "redrawboard" -> redrawBoard();
                case "leave" -> leaveGame();
                case "resign" -> resignPrompt();
                case "yes" -> resign();
                case "makemove" -> makeMove(params);
                case "highlightmoves" -> highlightMoves(params);
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String highlightMoves(String... params) throws Exception {
        if (state == State.IN_GAME) {
            if (params.length == 1) {
                String position = params[0];
                int col = (position.charAt(0) - 'a') + 1;
                int row = position.charAt(1) - '0';
                ChessGame game = currentGame.game();
                ChessBoard board = game.getBoard();
                if (board.getPiece(new ChessPosition(row, col)) == null) {
                    throw new ClientException(400, "No piece at position.");
                }
                Collection<ChessMove> moves = game.validMoves(new ChessPosition(row, col));
                for (ChessMove move : moves) {
                    positions.add(move.getEndPosition());
                }
                String returnString = redrawBoard();
                positions = new ArrayList<>();
                return returnString;
            } else {
                throw new ClientException(400, "Expected: highlightMoves <position>");
            }
        }
        throw new ClientException(400, "Cannot highlight moves, not in game");
    }

    public String makeMove(String ... params) throws Exception {
        if (state == State.IN_GAME) {
            if (params.length >= 2) {
                String start = params[0];
                String end = params[1];
                int startcol = (start.charAt(0) - 'a') + 1;
                int startrow = start.charAt(1) - '0';
                int endcol = (end.charAt(0) - 'a') + 1;
                int endrow = end.charAt(1) - '0';
                ChessPiece.PieceType type = null;
                if (params.length == 3) {
                    switch (params[2]) {
                        case "queen" -> type = ChessPiece.PieceType.QUEEN;
                        case "knight" -> type = ChessPiece.PieceType.KNIGHT;
                        case "bishop" -> type = ChessPiece.PieceType.BISHOP;
                        case "rook" -> type = ChessPiece.PieceType.ROOK;
                    }
                }
                ChessPosition startPosition = new ChessPosition(startrow, startcol);
                ChessPosition endPosition = new ChessPosition(endrow, endcol);
                ChessGame game = currentGame.game();
                ChessBoard board = game.getBoard();
                if (board.getPiece(startPosition) == null) {
                    throw new ClientException(400, "No piece at position.");
                }
                Collection<ChessMove> moves = game.validMoves(startPosition);
                ChessMove move = new ChessMove(startPosition, endPosition, type);
                if (!moves.contains(move)) {
                    throw new ClientException(400, "Cannot make move.");
                }
                ws.makeMove(authData, currentGame.gameID(), move);
                return "";
            } else {
                throw new ClientException(400, "Expected: makeMove <starting position> <ending position> <promotionpiece>");
            }
        }
        throw new ClientException(400, "Not in game, cannot make a move");
    }

    public String resign() throws Exception {
        ws.resignGame(authData, currentGame.gameID());
        return "";
    }

    public String resignPrompt() throws Exception {
        return "Are you sure you would like to resign? If so, reply 'yes' ";
    }

    public String leaveGame() throws Exception {
        if (state == State.IN_GAME || state == State.OBSERVING_GAME) {
            try {
                ws.leaveGame(authData, currentGame.gameID());
                ws = null;
                state = State.SIGNED_IN;
                return "Left game successfully.";
            } catch (Exception ex) {
                throw new ClientException(500, "Error leaving game");
            }
        }
        throw new ClientException(400, "Please join a game first!!");
    }

    public String redrawBoard() throws Exception {
        if (state == State.IN_GAME || state == State.OBSERVING_GAME) {
            return gameToString(currentGame, currentColor);
        }
        throw new ClientException(400, "Please join a game first!!");
    }

    public String playGame(String... params) throws Exception {
        if (state == State.SIGNED_IN) {
            if (isNull(this.games)) {
                throw new ClientException(400, "Make sure to check all games by using 'listGames' first!");
            }
            int gameNumber = 0;
            try {
                gameNumber = Integer.parseInt(params[0]);
            } catch (Exception ex) {
                throw new ClientException(400, "Number not given for GameNumber.");
            }
            if (params.length == 2) {
                currentGameNumber =  gameNumber - 1;
                currentGame = games.get(currentGameNumber);
                currentColor = params[1].toUpperCase();
                server.joinGame(new JoinGameData(currentColor, currentGame.gameID()), authData.authToken());
                state = State.IN_GAME;
                ws = new WebSocketFacade(serverUrl, notificationHandler);
                ws.joinGame(authData, currentGame.gameID());
                return "Good Luck!";
            } else {
                throw new ClientException(400, "Expected: <GameNumber> <Color>");
            }
        }
        throw new ClientException(400, "Logged out, cannot perform command.");
    }

    public String observeGame(String... params) throws Exception {
        if (state == State.SIGNED_IN) {
            if (isNull(this.games)) {
                throw new ClientException(400, "Make sure to check all games by using 'listGames' first!");
            }
            int gameNumber = 0;
            try {
                gameNumber = Integer.parseInt(params[0]);
            } catch (Exception ex) {
                throw new ClientException(400, "Number not given for GameNumber.");
            }
            if(gameNumber > games.size()) {
                throw new ClientException(400, "Incorrect game number. Please check the game number and try again.");
            }
            currentGameNumber =  gameNumber - 1;
            currentGame = games.get(currentGameNumber);
            currentColor = "WHITE";
            state = State.OBSERVING_GAME;
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.joinGame(authData, currentGame.gameID());
            return "Enjoy!";
        } else if (state == State.IN_GAME || state == State.OBSERVING_GAME) {
            throw new ClientException(400, "Leave game first to observe new game");
        }
        throw new ClientException(400, "Logged out, cannot perform command.");
    }

    public String listGames() throws Exception {
        if (state == State.SIGNED_IN) {
            fetchGames();
            StringBuilder result = new StringBuilder("Games\n");
            for (int i = 0; i < games.size(); i++) {
                GameData game = games.get(i);
                result.append(i + 1)
                        .append(": ").append(game.gameName())
                        .append(", White Player: ").append(game.whiteUsername() == null ? "None" : game.whiteUsername())
                        .append(", Black Player: ").append(game.blackUsername() == null ? "None" : game.blackUsername())
                        .append("\n");
            }
            result.append("\nIf a color's player is 'None' on a game, game is joinable with that color.");
            return result.toString();
        }
        throw new ClientException(400, "Logged out, cannot perform command.");
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
                return "Game created successfully!\nList Games to see available games!";
            }
            throw new ClientException(500, "Expected: <GameName>");
        }
        throw new ClientException(400, "Logged out, cannot perform command.");
    }

    public String registerUser(String... params) throws Exception {
        if (params.length == 3) {
            UserData user = new UserData(params[0], params[1], params[2]);
            AuthData auth;
            auth = server.registerUser(user);
            if (auth != null) {
                authData = auth;
                state = State.SIGNED_IN;
                return "Registered and logged in successfully as " + authData.username() + "!";
            }
        }
        throw new ClientException(400, "Expected: <Username> <Password> <Email>");
    }

    public String loginUser(String... params) throws Exception {
        if(authData != null) {
            if (Objects.equals(params[0], authData.username())) {
                throw new ClientException(400, "Already logged in!!");
            }
        }
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
                return "Logged in successfully as " + authData.username() + "!";
            }
        }
        throw new ClientException(400, "Expected: <Username> <Password>");
    }

    public String logoutUser() throws Exception {
        if (state == State.SIGNED_IN) {
            try {
                server.logoutUser(authData.authToken());
                state = State.SIGNED_OUT;
                authData = null;
                return "Logged out successfully!";
            } catch (Exception ex) {
                throw new ClientException(500, ex.getMessage());
            }
        }
        throw new ClientException(400, "Logged out, cannot perform command.");
    }

    private void fetchGames() throws Exception {
        games = server.fetchAllGames(authData.authToken());
    }

    private String gameToString(GameData gameData, String color) throws Exception {
        gameString = new StringBuilder();
        ChessGame game = gameData.game();
        ChessBoard board = game.getBoard();
        if(Objects.equals(color, "WHITE")) {
            buildBackwardsBoard(board);
        } else if (Objects.equals(color, "BLACK")) {
            buildBoard(board);
        }
        return gameString.toString();
    }

    private boolean checkPosition(int row, int col) {
        ChessPosition positionToCheck = new ChessPosition(row, col);
        return positions.contains(positionToCheck);
    }

    private void checkThePosition(Integer i, Integer j, String color) {
        if (checkPosition(i, j)) {
            if (Objects.equals(color, "BLACK")) {
                gameString.append(SET_BG_COLOR_DARK_GREEN);
            } else {
                gameString.append(SET_BG_COLOR_GREEN);
            }
        }
    }

    private void buildBoard(ChessBoard board) {
        for (int i = 0; i < 10; i++) {
            if (i == 0 || i == 9) {
                gameString.append(SET_BG_COLOR_DARK_GREY
                        +  SET_TEXT_COLOR_WHITE + "    h   g   f  e   d   c  b   a    " + RESET_BG_COLOR + "\n");
            } else if (i % 2 != 0) {
                for (int j = 9; j >= 0; j--) {
                    if (j == 0 || j == 9) {
                        gameString.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " ").append(i).append(" ").append(RESET_BG_COLOR);
                    } else if (j % 2 != 0) {
                        gameString.append(SET_BG_COLOR_BLACK);
                        checkThePosition(i, j, "BLACK");
                        addPiece(board, i, j);
                    } else {
                        gameString.append(SET_BG_COLOR_LIGHT_GREY);
                        checkThePosition(i, j, "GREY");
                        addPiece(board, i, j);
                    }
                }
                gameString.append("\n");
            } else {
                for (int k = 9; k >= 0; k--) {
                    if (k == 0 || k == 9) {
                        gameString.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " ").
                                append(i).append(" ").append(RESET_BG_COLOR);
                    } else if (k % 2 != 0) {
                        gameString.append(SET_BG_COLOR_LIGHT_GREY);
                        checkThePosition(i, k, "GREY");
                        addPiece(board, i, k);
                    } else {
                        gameString.append(SET_BG_COLOR_BLACK);
                        checkThePosition(i, k, "BLACK");
                        addPiece(board, i, k);
                    }
                }
                gameString.append("\n");
            }
        }
    }

    private void buildBackwardsBoard(ChessBoard board) {
        for (int i = 9; i >= 0; i--) { // Loop backwards for rows
            if (i == 9 || i == 0) {
                gameString.append(SET_BG_COLOR_DARK_GREY
                        + SET_TEXT_COLOR_WHITE + "    a   b   c  d   e   f  g   h    " + RESET_BG_COLOR + "\n");
            } else if (i % 2 != 0) {
                for (int t = 0; t < 10; t++) { // Loop backwards for columns
                    if (t == 9 || t == 0) {
                        gameString.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " ").append(i).append(" ").append(RESET_BG_COLOR);
                    } else if (t % 2 != 0) {
                        gameString.append(SET_BG_COLOR_BLACK);
                        checkThePosition(i, t, "BLACK");
                        addPiece(board, i, t);
                    } else {
                        gameString.append(SET_BG_COLOR_LIGHT_GREY);
                        checkThePosition(i, t, "GREY");
                        addPiece(board, i, t);
                    }
                }
                gameString.append("\n");
            } else {
                for (int e = 0; e < 10; e++) { // Loop backwards for columns
                    if (e == 9 || e == 0) {
                        gameString.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " ")
                                .append(i).append(" ").append(RESET_BG_COLOR);
                    } else if (e % 2 != 0) {
                        gameString.append(SET_BG_COLOR_LIGHT_GREY);
                        checkThePosition(i, e, "GREY");
                        addPiece(board, i, e);
                    } else {
                        gameString.append(SET_BG_COLOR_BLACK);
                        checkThePosition(i, e, "BLACK");
                        addPiece(board, i, e);
                    }
                }
                gameString.append("\n");
            }
        }
    }

    private void addPiece(ChessBoard board, int i, int j) {
        if (board.getPiece(new ChessPosition(i, j)) == null) {
            gameString.append(EMPTY);
        } else {
            ChessPiece piece = board.getPiece(new ChessPosition(i, j));
            switch(piece.type) {
                case KING -> {
                    switch (piece.pieceColor) {
                        case BLACK -> gameString.append(WHITE_KING);
                        case WHITE -> gameString.append(BLACK_KING);
                    }
                } case QUEEN -> {
                    switch (piece.pieceColor) {
                        case BLACK -> gameString.append(WHITE_QUEEN);
                        case WHITE -> gameString.append(BLACK_QUEEN);
                    }
                } case ROOK -> {
                    switch (piece.pieceColor) {
                        case BLACK -> gameString.append(WHITE_ROOK);
                        case WHITE -> gameString.append(BLACK_ROOK);
                    }
                } case BISHOP -> {
                    switch (piece.pieceColor) {
                        case BLACK -> gameString.append(WHITE_BISHOP);
                        case WHITE -> gameString.append(BLACK_BISHOP);
                    }
                } case KNIGHT -> {
                    switch (piece.pieceColor) {
                        case BLACK -> gameString.append(WHITE_KNIGHT);
                        case WHITE -> gameString.append(BLACK_KNIGHT);
                    }
                } case PAWN -> {
                    switch (piece.pieceColor) {
                        case BLACK -> gameString.append(WHITE_PAWN);
                        case WHITE -> gameString.append(BLACK_PAWN);
                    }
                }
            }
        }
    }

    public String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    Please use one of the following commands:
                    - register <Username> <Password> <Email> - create an account
                    - login <Username> <Password> - to play chess
                    - quit - to exit
                    - help - get possible commands""";
        } else if (state == State.IN_GAME) {
            return """
                    You can use one of the following commands:
                    - redrawBoard - redraws the chess board
                    - leave - leave the game you are playing or observing
                    - makeMove <starting position> <ending position> <promotion piece> - make a move
                                            for <promotion piece> put piece you want pawn to be if getting promoted
                                            if not getting promoted leave blank
                    - resign - resign and forfeit game
                    - highlightMoves <position> - highlight the legal moves for a piece at a certain position""";
        }
        return """
                Please use one of the following commands:
                - logout
                - createGame <GameName> - to create a game
                - listGames - to list all games
                - playGame <GameNumber> <Color> - to play a game
                - observeGame <GameNumber> - to watch a game
                - quit - to exit
                - help - get possible commands""";
    }

    public String getState() {
        return state.toString();
    }
}