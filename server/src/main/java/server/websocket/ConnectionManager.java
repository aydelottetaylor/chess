package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(Integer gameId, String authToken, Session session) {
        var connection = new Connection(gameId, authToken, session);
        connections.put(authToken, connection);
    }

    public void remove(Integer gameId) {
        connections.remove(gameId);
    }

    public void broadcast(Integer gameId, ServerMessage notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        Gson gson = new Gson();

        for (var c : connections.values()) {
            System.out.println(c.gameId);
            if (c.session.isOpen()) {
                if (c.gameId.equals(gameId)) {
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
