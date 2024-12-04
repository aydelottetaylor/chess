package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(Integer gameId, String authToken, Session session) {
        var connection = new Connection(gameId, authToken, session);
        connections.put(authToken, connection);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void sendGame(LoadGameMessage message, String authToken) throws IOException {
        var connection = connections.get(authToken);
        if (connection != null) {
            Gson gson = new Gson();
            String jsonNotification = gson.toJson(message);
            connection.send(jsonNotification);
        }
    }

    public void sendErrorMessage(ErrorMessage message, String authToken) throws IOException {
        var connection = connections.get(authToken);
        if (connection != null) {
            Gson gson = new Gson();
            String jsonNotification = gson.toJson(message);
            connection.send(jsonNotification);
        }
    }

    public void broadcast(Integer gameId, ServerMessage notification, String authToken) throws IOException {
        var removeList = new ArrayList<Connection>();
        Gson gson = new Gson();

        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.gameId.equals(gameId) && !c.authToken.equals(authToken)) {
                    String jsonNotification = gson.toJson(notification);
                    c.send(jsonNotification);
                }
            } else {
                removeList.add(c);
            }
        }

        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }
}
