package ui.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.*;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;


import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public  WebSocketFacade(String url, NotificationHandler notificationHandler) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(ServerMessage.class, new ServerMessage.ServerMessageDeserializer())
                            .create();

                    try {
                        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

                        if (serverMessage instanceof ErrorMessage) {
                            notificationHandler.notify((ErrorMessage) serverMessage);
                        } else if (serverMessage instanceof NotificationMessage) {
                            notificationHandler.notify((NotificationMessage) serverMessage);
                        } else if (serverMessage instanceof LoadGameMessage) {
                            notificationHandler.notify((LoadGameMessage) serverMessage);
                        } else {
                            System.err.println("Unknown ServerMessage type: " + serverMessage.getServerMessageType());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to process message: " + e.getMessage());
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new WebSocketException(500, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinGame(AuthData authData, Integer gameId) throws WebSocketException {
        try {
            if (this.session == null || !this.session.isOpen()) {
                throw new IllegalStateException("WebSocket session is not open");
            }
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authData.authToken(), gameId);
            Gson gson = new Gson();
            String payload = gson.toJson(action);
            this.session.getBasicRemote().sendText(payload);
        } catch (IOException ex) {
            throw new WebSocketException(500, "Failed to send join game command");
        }
    }

    public void makeMove(AuthData authData, Integer gameId, ChessMove move) throws WebSocketException {
        try {
            if (this.session == null || !this.session.isOpen()) {
                throw new IllegalStateException("WebSocket session is not open");
            }
            var action = new MakeMoveCommand(authData.authToken(), gameId, move);
            Gson gson = new Gson();
            String payload = gson.toJson(action);
            this.session.getBasicRemote().sendText(payload);
        } catch (IOException ex) {
            throw new WebSocketException(500, "Failed to send move command");
        }
    }

    public void leaveGame(AuthData authData, Integer gameId) throws WebSocketException {
        try {
            if (this.session == null || !this.session.isOpen()) {
                throw new IllegalStateException("WebSocket session is not open");
            }
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authData.authToken(), gameId);
            Gson gson = new Gson();
            String payload = gson.toJson(action);
            this.session.getBasicRemote().sendText(payload);
            this.session.close();
        } catch (IOException ex) {
            throw new WebSocketException(500, "Failed to send leave game command");
        }
    }

    public void resignGame(AuthData authData, Integer gameId) throws WebSocketException {
        try {
            if (this.session == null || !this.session.isOpen()) {
                throw new IllegalStateException("WebSocket session is not open");
            }
            var action = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authData.authToken(), gameId);
            Gson gson = new Gson();
            String payload = gson.toJson(action);
            this.session.getBasicRemote().sendText(payload);
        } catch (IOException ex) {
            throw new WebSocketException(500, "Failed to send resign game command");
        }
    }
}