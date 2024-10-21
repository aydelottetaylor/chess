package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Collection;

public class AuthDAO implements AuthDataAccess {
    final private HashMap<String, AuthData> authsByUsername = new HashMap<>();
    final private HashMap<String, AuthData> authsByToken = new HashMap<>();

    public AuthData getAuthInfoByUsername(String username) {
        return authsByUsername.get(username);
    }

    public void addAuthToken(String username, String authToken) throws Exception {
        authsByUsername.put(username, new AuthData(username, authToken));
        authsByToken.put(authToken, new AuthData(username, authToken));
    }

    public AuthData getAuthInfoByToken(String authToken) {
        return authsByToken.get(authToken);
    }

    public void removeAuthorization(AuthData authData) {
        authsByUsername.remove(authData.username());
        authsByToken.remove(authData.authToken());
    }

    public void clearAuths() {
        authsByUsername.clear();
        authsByToken.clear();
    }

    public Collection<AuthData> getAllAuths() {
        return authsByUsername.values();
    }

}
