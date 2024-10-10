package server;

import spark.*;

public class Server {

    public Server() {

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::closeSession);
        Spark.get("/game", this:: getGame);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::deleteDatabase);

        // Spark.exception(ResponseException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    // private void exceptionHandler(ResponseException ex, Request req, Response res) {
    //     res.status(ex.StatusCode());
    // }

    private Object registerUser(Request req, Response res) {
        return "";
    }

    private Object login(Request req, Response res) {
        return "";
    }

    private Object closeSession(Request req, Response res) {
        return "";
    }

    private Object getGame(Request req, Response res) {
        return "";
    }

    private Object createGame(Request req, Response res) {
        return "";
    }

    private Object joinGame(Request req, Response res) {
        return "";
    }

    private Object deleteDatabase(Request req, Response res) {
        return "";
    }
}
