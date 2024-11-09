package ui;

import java.util.Arrays;

import com.google.gson.Gson;
import model.*;
import ui.websocket.NotificationHandler;
import server.ServerFacade;
import ui.websocket.WebSocketException;
import ui.websocket.WebSocketFacade;

public class Client {
    private AuthData authData;
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
                case "register" -> registerUser(params);
                case "login" -> loginUser(params);
                case "logout" -> logoutUser();
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
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
        throw new ClientException(400, "Expected: <username> <password> <email>");
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
        throw new ClientException(400, "Expected: <username> <password>");
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

    public void throwLoggedOut() throws Exception {
        throw new ClientException(400, "Logged out, cannot perform command.");
    }

    public String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    - register <username> <password> <email> - create an account
                    - login <username> <password> - to play chess
                    - quit - to exit
                    - help - get possible commands
                    """;
        }
        return """
                - logout
                - createGame <gameName> - to create a game
                - listGames - to list all games
                - playGame <gameNumber> <color> - to play a game
                - observeGame <gameNumber> - to watch a game
                - quit - to exit
                - help - get possible commands
                """;
    }



    public String getState() {
        return state.toString();
    }
}