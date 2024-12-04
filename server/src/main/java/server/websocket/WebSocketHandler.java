package server.websocket;

import com.google.gson.Gson;
import dataaccess.*;
import service.*;
import model.*;
import server.ServerException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
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
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);

        switch (action.getCommandType()) {
            case CONNECT -> connectGame(action.getAuthToken(), action.getGameID(), session);
            case MAKE_MOVE -> makeMove(action.getAuthToken(), action.getGameID(), action.getMove());
            case LEAVE -> leaveGame(action.getAuthToken(), action.getGameID(), session);
            case RESIGN -> resignGame(action.getAuthToken(), action.getGameID(), session);
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

    private void makeMove(String visitorName) {

    }

    private void leaveGame(String authToken, Integer gameId, Session session) throws ServerException {
        try {
            connections.remove(authToken);
            UserData user = userService.getUserOnAuthToken(authToken);
            var message = String.format("User %s left the game!", user.getUsername());
            var notification = new NotificationMessage(message);
            connections.broadcast(gameId, notification, authToken);
        } catch (Exception ex) {
            throw new ServerException(400, "WebsocketHandler Leave Game Error " + ex.getMessage());
        }
    }

    private void resignGame(String authToken, Integer gameId, Session session) throws ServerException {
        try {
            UserData user = userService.getUserOnAuthToken(authToken);
            var message = String.format("User %s has resigned!", user.getUsername());
            var notification = new NotificationMessage(message);
            connections.broadcast(gameId, notification, null);
        } catch (Exception ex) {
            throw new ServerException(400, "WebsocketHandler Resign Game Error " + ex.getMessage());
        }
    }
}