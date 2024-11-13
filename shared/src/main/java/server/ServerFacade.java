package server;

import model.*;
import com.google.gson.*;
import java.util.List;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

//    Takes user data and registers user, gives back auth data
    public AuthData registerUser(UserData user) throws ServerFacadeException {
        var path = "/user";
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

//    Takes user data and logs in user. gives back auth data
    public AuthData loginUser(UserData user) throws ServerFacadeException {
        var path = "/session";
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

//    Takes authtoken and logs out user, does not return
    public void logoutUser(String authToken) throws ServerFacadeException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }

//    Takes Game Data and authtoken and creates game, sends back game data
    public Object createGame(GameData game, String authToken) throws ServerFacadeException {
        var path = "/game";
        return this.makeRequest("POST", path, game, Object.class, authToken);
    }

//    Takes authtoken and returns a list of Games
    public List<GameData> fetchAllGames(String authToken) throws ServerFacadeException {
        var path = "/game";
        GameResponse gameResponse = this.makeRequest("GET", path, null, GameResponse.class, authToken);
        return gameResponse.getGames();
    }

//    Takes JoinGameData and authtoken, joins user to game as requested color and returns nothing
    public void joinGame(JoinGameData game, String authToken) throws ServerFacadeException {
        var path = "/game";
        this.makeRequest("PUT", path, game, null, authToken);
    }

//    Takes nothing and returns nothing, clears database
    public void clear() throws ServerFacadeException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ServerFacadeException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ServerFacadeException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ServerFacadeException {
        var status = http.getResponseCode();
        var message = readStream(http.getErrorStream());
        if (!isSuccessful(status)) {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "No message found";
            throw new ServerFacadeException(status, message);
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) { // Handle cases where the stream might be null
            return "";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        return response.toString();
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;

        // Read the response body from the connection
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);

                if (responseClass == String.class) {
                    // If expecting a raw String, read the entire stream and return as String
                    StringBuilder result = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    return responseClass.cast(result.toString());
                } else if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}