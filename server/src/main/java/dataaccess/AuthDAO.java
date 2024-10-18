package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Collection;
import java.util.Random;

public class AuthDAO implements AuthDataAccess {
    final private HashMap<String, AuthData> auths = new HashMap<>();
    final private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    final private static int AUTHLENGTH = 20;

    public AuthData getAuthInfo(String username) {
        return auths.get(username);
    }

    public void addAuthToken(String username) throws Exception{
        String authToken = generateAuthToken();
        auths.put(username, new AuthData(username, authToken));
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
