package service;

import dataaccess.*;
import model.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

public class UserService {
    private final UserDataAccess userDataAccess;
    private final AuthDataAccess authDataAccess;
    final private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    final private static int AUTHLENGTH = 20;

    public UserService() {
        this.userDataAccess = new UserDAO();
        this.authDataAccess = new AuthDAO();
    }

    public AuthData registerUser(UserData newUser) throws Exception {
            if(newUser.username() == null || newUser.password() == null || newUser.email() == null) {
                throw new ServiceException(400, "Error: bad request");
            }

            if (userDataAccess.getUser(newUser.username()) != null) {
                throw new ServiceException(403, "Error: already taken");
            } else {
                userDataAccess.addUser(newUser);
                authDataAccess.addAuthToken(newUser.username(), generateAuthToken());
            }

        return authDataAccess.getAuthInfoByUsername(newUser.username());
    }

    public AuthData loginUser(UserData userInfo) throws Exception {
        UserData user = userDataAccess.getUser(userInfo.username());
        if (user == null) {
            throw new ServiceException(401, "Error: unauthorized, no matching user registered");
        }
        if (Objects.equals(user.password(), userInfo.password())) {
            authDataAccess.addAuthToken(userInfo.username(), generateAuthToken());
            return authDataAccess.getAuthInfoByUsername(userInfo.username());
        } else {
            throw new ServiceException(401, "Error: unauthorized, wrong password");
        }
    }

    public void logoutUser(String authToken) throws Exception {
        authorizeUser(authToken);
        authDataAccess.removeAuthorization(authDataAccess.getAuthInfoByToken(authToken));
    }

    public void authorizeUser(String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuthInfoByToken(authToken);
        if (auth == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
    }

    public void clearUsersAndAuths() throws Exception {
        userDataAccess.clearUsers();
        authDataAccess.clearAuths();
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

    public UserData getUserOnAuthToken(String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuthInfoByToken(authToken);
        return userDataAccess.getUser(auth.username());
    }

    public UserData getUserOnUserName(String username) {
        return userDataAccess.getUser(username);
    }

    public AuthData getAuthInfoByUsername(String username) {
        return authDataAccess.getAuthInfoByUsername(username);
    }

    public Collection<UserData> getAllUsers() {
        return userDataAccess.listUsers();
    }

    public Collection<AuthData> getAllAuths() {
        return authDataAccess.getAllAuths();
    }

}
