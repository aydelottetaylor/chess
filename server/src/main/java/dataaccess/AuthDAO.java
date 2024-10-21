package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Collection;

public class AuthDAO implements AuthDataAccess {
    final private HashMap<String, AuthData> authsByUsername = new HashMap<>();
    final private HashMap<String, AuthData> authsByToken = new HashMap<>();

    // Takes username, returns authorization data associated with username
    public AuthData getAuthInfoByUsername(String username) {
        return authsByUsername.get(username);
    }

    // Takes username and authToken and adds new authorization data to stored data
    public void addAuthToken(String username, String authToken) throws Exception {
        authsByUsername.put(username, new AuthData(username, authToken));
        authsByToken.put(authToken, new AuthData(username, authToken));
    }

    // Takes authToken and returns authorization data associated with that authToken
    public AuthData getAuthInfoByToken(String authToken) {
        return authsByToken.get(authToken);
    }

    // Takes authData and removes authorization information associated with that authToken and username
    public void removeAuthorization(AuthData authData) {
        authsByUsername.remove(authData.username());
        authsByToken.remove(authData.authToken());
    }

    // Clears all authorization data in stored data
    public void clearAuths() {
        authsByUsername.clear();
        authsByToken.clear();
    }

    // Returns all authorization data
    public Collection<AuthData> getAllAuths() {
        return authsByUsername.values();
    }

}
