package server;

import com.google.gson.Gson;
import service.*;
import dataaccess.*;
import spark.*;
import model.*;

import java.util.Map;

public class Server {
    private final Gson serializer = new Gson();
    private final UserService userService = new UserService();
    private final GameService gameService = new GameService(userService);

    public Server() {
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::closeSession);
        Spark.get("/game", this:: getGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::deleteDatabase);
        Spark.exception(ServiceException.class, this::serviceExceptionHandler);
        Spark.exception(DataAccessException.class, this::dataAccessExceptionHandler);
        Spark.exception(ServerException.class, this::serverException);

        Spark.notFound((req, res) -> {
            res.type("application/json");
            res.status(404);
            return serializer.toJson(Map.of("message", "Route not found"));
        });

        Spark.before((req, res) -> res.type("application/json"));

        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    // Handles Server Exceptions
    private void serverException(ServerException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.body(serializer.toJson(Map.of("message", ex.getMessage())));
    }

    // Handles Data Access Exceptions
    private void dataAccessExceptionHandler(DataAccessException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.body(serializer.toJson(Map.of("message", ex.getMessage())));
    }

    // Handles Service Exceptions
    private void serviceExceptionHandler(ServiceException ex, Request req, Response res) {
         res.status(ex.statusCode());
         res.body(serializer.toJson(Map.of("message", ex.getMessage())));
     }

    // Register a new user, does not require authentication, requires username, password, and email
    private Object registerUser(Request req, Response res) throws Exception {
        var newUser = serializer.fromJson(req.body(), UserData.class);
        var result = userService.registerUser(newUser);
        return serializer.toJson(result);
    }

    // Login existing user, does not require authentication, requires username and password
    private Object login(Request req, Response res) throws Exception{
        var userInfo = serializer.fromJson(req.body(), UserData.class);
        var result = userService.loginUser(userInfo);
        return serializer.toJson(result);
    }

    // Logout currently logged in user, requires authentication
    private Object closeSession(Request req, Response res) throws Exception {
        var authToken = req.headers("Authorization");
        userService.logoutUser(authToken);
        return serializer.toJson(null);
    }

    // Get all existing games and their information, requires authentication
    private Object getGames(Request req, Response res) throws Exception {
        var authToken = req.headers("Authorization");
//        var response = gameService.getAllGames(authToken);
//        return serializer.toJson(response);
        return "";
    }

    // Create a new game, requires authentication and game name
    private Object createGame(Request req, Response res) throws Exception {
        var authToken = req.headers("Authorization");
        var gameInfo = serializer.fromJson(req.body(), GameData.class);
        var result = gameService.createGame(gameInfo, authToken);
        Map<String, Integer> response = Map.of("gameID", result.gameID());
        return serializer.toJson(response);
    }

    // Join an existing game, requires authentication and gameID
    private Object joinGame(Request req, Response res) throws Exception {
        var authToken = req.headers("Authorization");
        var reqInfo = serializer.fromJson(req.body(), JoinGameData.class);
        gameService.joinGame(authToken, reqInfo);
        return serializer.toJson(null);
    }

    // Delete all existing user, authorization, and game data, does not require authentication
    private Object deleteDatabase(Request req, Response res) throws Exception {
        gameService.clearDatabase();
        return serializer.toJson(null);
    }
}
