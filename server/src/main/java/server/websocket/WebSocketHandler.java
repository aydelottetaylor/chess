package server.websocket;

import com.google.gson.Gson;
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


    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()) {
            case CONNECT -> connectGame(action.getAuthToken(), action.getGameID(), session);
            case MAKE_MOVE -> makeMove("temp");
            case LEAVE -> leaveGame();
            case RESIGN -> resignGame();
        }
    }

    private void connectGame(String authToken, Integer gameId, Session session) throws ServerException {
        try {
            connections.add(gameId, authToken, session);
            UserData user = userService.getUserOnAuthToken(authToken);
            var message = String.format("User %s joined the game!", user.getUsername());
            var notification = new NotificationMessage(message);
            connections.broadcast(gameId, notification);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void makeMove(String visitorName) {

    }

    private void leaveGame() {

    }

    private void resignGame() {

    }
}