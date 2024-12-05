package server.websocket;

import chess.*;
import com.google.gson.*;
import dataaccess.*;
import service.*;
import model.*;
import server.ServerException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Timer;

import static websocket.commands.UserGameCommand.CommandType.*;


@WebSocket
public class WebSocketHandler {
    private UserService userService = new UserService();
    private GameDataAccess gameDataAccess = new MySQLGameDAO();
    private AuthDataAccess authDataAccess = new MySQLAuthDAO();


    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
                .create();

        UserGameCommand action = gson.fromJson(message, UserGameCommand.class);

        switch (action.getCommandType()) {
            case CONNECT -> connectGame(action.getAuthToken(), action.getGameID(), session);
            case MAKE_MOVE -> {
                if (action instanceof MakeMoveCommand) {
                    MakeMoveCommand makeMoveAction = (MakeMoveCommand) action;
                    makeMove(makeMoveAction.getAuthToken(), makeMoveAction.getGameID(), makeMoveAction.getMove(), session);
                } else {
                    System.err.println("Invalid MAKE_MOVE command received.");
                }
            }
            case LEAVE -> leaveGame(action.getAuthToken(), action.getGameID(), session);
            case RESIGN -> resignGame(action.getAuthToken(), action.getGameID(), session);
        }
    }

    public class UserGameCommandDeserializer implements JsonDeserializer<UserGameCommand> {
        @Override
        public UserGameCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            // Get the commandType field
            String commandTypeString = jsonObject.get("commandType").getAsString();
            UserGameCommand.CommandType commandType = UserGameCommand.CommandType.valueOf(commandTypeString);

            // Switch to handle specific subclasses
            switch (commandType) {
                case MAKE_MOVE:
                    return new Gson().fromJson(json, MakeMoveCommand.class); // Deserialize as MakeMoveCommand
                default:
                    return new Gson().fromJson(json, UserGameCommand.class); // Default to UserGameCommand
            }
        }
    }

    private void connectGame(String authToken, Integer gameId, Session session) throws ServerException {
        try {
            connections.add(gameId, authToken, session);

            // Send game
            GameData game = gameDataAccess.getGameById(gameId);
            AuthData auth = authDataAccess.getAuthInfoByToken(authToken);

            if (game == null) {
                var message = String.format("ERROR: Invalid game id ");
                var notification = new ErrorMessage(message);
                connections.sendErrorMessage(notification, authToken);
            } else if (auth == null) {
                var message = String.format("ERROR: Invalid auth token ");
                var notification = new ErrorMessage(message);
                connections.sendErrorMessage(notification, authToken);
            } else {
                var gameNotification = new LoadGameMessage(game);
                connections.sendGame(gameNotification, authToken);

                // Send message
                UserData user = userService.getUserOnAuthToken(authToken);
                var message = String.format("User %s joined the game!", user.getUsername());
                var notification = new NotificationMessage(message);
                connections.broadcast(gameId, notification, authToken);
            }
        } catch (Exception ex) {
            throw new ServerException(400, "WebsocketHandler Connect Game Error " + ex.getMessage());
        }
    }

    private void makeMove(String authToken, Integer gameId, ChessMove move, Session session) throws ServerException {
        try {
            AuthData auth = authDataAccess.getAuthInfoByToken(authToken);
            if (auth == null) {
                var message = String.format("ERROR: Invalid auth token ");
                connections.sendErrorMessageNoAuth(message, session);
            } else {
                GameData game = gameDataAccess.getGameById(gameId);
                ChessPosition start = move.getStartPosition();
                ChessGame chessGame = game.getGame();
                ChessBoard board = chessGame.getBoard();

                if (board.getPiece(new ChessPosition(0, 0)) != null) {
                    var message = String.format("ERROR: Cannot move, game has been resigned.");
                    var notification = new ErrorMessage(message);
                    connections.sendErrorMessage(notification, authToken);
                    return;
                }

                if (board.getPiece(start).pieceColor == ChessGame.TeamColor.BLACK) {
                    if (!Objects.equals(auth.username(), game.blackUsername())) {
                        var message = String.format("ERROR: Cannot make move for other color or as observer");
                        var notification = new ErrorMessage(message);
                        connections.sendErrorMessage(notification, authToken);
                        return;
                    }
                } else if (board.getPiece(start).pieceColor == ChessGame.TeamColor.WHITE) {
                    if (!Objects.equals(auth.username(), game.whiteUsername())) {
                        var message = String.format("ERROR: Cannot make move for other color or as observer");
                        var notification = new ErrorMessage(message);
                        connections.sendErrorMessage(notification, authToken);
                        return;
                    }
                }

                if(chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                    var message = String.format("ERROR: Cannot make move, %s is checkmate!", game.blackUsername());
                    var notification = new ErrorMessage(message);
                    connections.sendErrorMessage(notification, authToken);
                    return;
                } else if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                    var message = String.format("ERROR: Cannot make move, %s is checkmate!", game.whiteUsername());
                    var notification = new ErrorMessage(message);
                    connections.sendErrorMessage(notification, authToken);
                    return;
                } else if (chessGame.isInStalemate(ChessGame.TeamColor.BLACK) || chessGame.isInStalemate(ChessGame.TeamColor.WHITE)) {
                    var message = String.format("ERROR: Cannot make move, is in stalemate!");
                    var notification = new ErrorMessage(message);
                    connections.sendErrorMessage(notification, authToken);
                    return;
                }

                chessGame.makeMove(move);

                GameData newGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame);
                gameDataAccess.setGameById(gameId, newGame);

                game = gameDataAccess.getGameById(gameId);

                var gameNotification = new LoadGameMessage(game);
                connections.broadcastGame(gameNotification, gameId);

                UserData user = userService.getUserOnAuthToken(authToken);
                var message = String.format("User %s has made their move.", user.getUsername());
                var notification = new NotificationMessage(message);
                connections.broadcast(gameId, notification, authToken);
            }
        } catch (Exception ex) {
            try {
                var notification = new ErrorMessage(ex.getMessage());
                connections.sendErrorMessage(notification, authToken);
            } catch (IOException e) {
                throw new ServerException(400, "WebsocketHandler Make Move Error " + ex.getMessage());
            }
        }
    }

    private void leaveGame(String authToken, Integer gameId, Session session) throws ServerException {
        try {
            GameData game = gameDataAccess.getGameById(gameId);
            UserData user = userService.getUserOnAuthToken(authToken);

            if (Objects.equals(user.username(), game.blackUsername())) {
                gameDataAccess.addUserToGame("", new JoinGameData("BLACK", gameId));
            } else if (Objects.equals(user.username(), game.whiteUsername())) {
                gameDataAccess.addUserToGame("", new JoinGameData("WHITE", gameId));
            }

            connections.remove(authToken);
            var message = String.format("User %s left the game!", user.getUsername());
            var notification = new NotificationMessage(message);
            connections.broadcast(gameId, notification, authToken);
        } catch (Exception ex) {
            throw new ServerException(400, "WebsocketHandler Leave Game Error " + ex.getMessage());
        }
    }

    private void resignGame(String authToken, Integer gameId, Session session) throws ServerException {
        try {
            GameData game = gameDataAccess.getGameById(gameId);
            ChessGame chessGame = game.getGame();
            ChessBoard chessBoard = chessGame.getBoard();
            UserData user = userService.getUserOnAuthToken(authToken);

            if (chessBoard.getPiece(new ChessPosition(0, 0)) != null) {
                var message = String.format("ERROR: Cannot resign, already resigned.");
                var notification = new ErrorMessage(message);
                connections.sendErrorMessage(notification, authToken);
                return;
            }

            if (!Objects.equals(user.username(), game.whiteUsername()) && !Objects.equals(user.username(), game.blackUsername())) {
                var message = String.format("ERROR: Cannot resign game as observer");
                var notification = new ErrorMessage(message);
                connections.sendErrorMessage(notification, authToken);
                return;
            }

            // Mark game as resigned
            chessBoard.addPiece(new ChessPosition(0, 0), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            chessGame.setBoard(chessBoard);
            GameData newGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame);
            gameDataAccess.setGameById(gameId, newGame);

            var message = String.format("User %s has resigned!", user.getUsername());
            var notification = new NotificationMessage(message);
            connections.broadcast(gameId, notification, null);
        } catch (Exception ex) {
            throw new ServerException(400, "WebsocketHandler Resign Game Error " + ex.getMessage());
        }
    }
}