package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Collection;
import java.util.Random;

public class AuthDAO implements AuthDataAccess {
    final private HashMap<String, AuthData> authsByUsername = new HashMap<>();
    final private HashMap<String, AuthData> authsByToken = new HashMap<>();
    final private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    final private static int AUTHLENGTH = 20;

    public AuthData getAuthInfoByUsername(String username) {
        return authsByUsername.get(username);
    }

    public void addAuthToken(String username) throws Exception {
        String authToken = generateAuthToken();
        authsByUsername.put(username, new AuthData(username, authToken));
        authsByToken.put(authToken, new AuthData(username, authToken));
    }

    public AuthData getAuthInfoByToken(String authToken) {
        System.out.println(authsByToken);
        return authsByToken.get(authToken);
    }

    public void removeAuthorization(AuthData authData) {
        authsByUsername.remove(authData.username());
        authsByToken.remove(authData.authToken());
    }

    private String generateAuthToken() throws DataAccessException {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(AUTHLENGTH);

        for (int i = 0; i < AUTHLENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }

}
