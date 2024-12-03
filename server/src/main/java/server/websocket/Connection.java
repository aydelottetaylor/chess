package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public Integer gameId;
    public String authToken;
    public Session session;

    public Connection(Integer gameId, String authToken, Session session) {
        this.gameId = gameId;
        this.authToken = authToken;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}