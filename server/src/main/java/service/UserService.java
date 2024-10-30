package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Random;

public class UserService {
    private final UserDataAccess userDataAccess;
    private final AuthDataAccess authDataAccess;
    final private static String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    final private static int AUTHLENGTH = 20;

    public UserService() {
        this.userDataAccess = new MySQLUserDAO();
        this.authDataAccess = new MySQLAuthDAO();
        MySQLUserDAO.createDatabase();
    }

    // Checks passed data to make user all data exists, checks that username 
    // doesn't already exist, adds user information to stored users
    public AuthData registerUser(UserData newUser) throws Exception {
            if(newUser.username() == null || newUser.password() == null || newUser.email() == null) {
                throw new ServiceException(400, "Error: bad request");
            }

            if (userDataAccess.getUser(newUser.username()) != null) {
                throw new ServiceException(403, "Error: already taken");
            } else {
                userDataAccess.addUser(newUser, BCrypt.hashpw(newUser.password(), BCrypt.gensalt()));
                authDataAccess.addAuthToken(newUser.username(), generateAuthToken());
            }

        return authDataAccess.getAuthInfoByUsername(newUser.username());
    }

    // Takes user username and password, checks password given with password stored for user
    // if password is correct calls to get authToken and adds authData to stored data
    public AuthData loginUser(UserData userInfo) throws Exception {
        UserData user = userDataAccess.getUser(userInfo.username());
        if (user == null) {
            throw new ServiceException(401, "Error: unauthorized, no matching user registered");
        }
        if (BCrypt.checkpw(userInfo.password(), user.password())) {
            String token = generateAuthToken();
            authDataAccess.addAuthToken(userInfo.username(), token);
            return new AuthData(userInfo.username(), token);
        } else {
            throw new ServiceException(401, "Error: unauthorized, wrong password");
        }
    }

    // Checks user authorization, removes authData from stored data to logout
    public void logoutUser(String authToken) throws Exception {
        authorizeUser(authToken);
        authDataAccess.removeAuthorization(authToken);
    }

    // Checks that authToken given matches existing auth token, if doesn't match throws exception
    public void authorizeUser(String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuthInfoByToken(authToken);
        if (auth == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
    }

    // Clears all user data from stored data
    public void clearUsers() throws Exception {
        userDataAccess.clearUsers();
    }

    // Clears all auth data from stored data
    public void clearAuths() {
        authDataAccess.clearAuths();
    }

    // Generates and returns 20 character alphanumeric authToken
    private String generateAuthToken() throws Exception {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(AUTHLENGTH);

        for (int i = 0; i < AUTHLENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }

    // Takes authToken and returns userData associated with that authToken
    public UserData getUserOnAuthToken(String authToken) throws Exception {
        if (authToken == null) {
            throw new ServiceException(500, "Error: AuthToken passed into getUserOnAuthToken is null");
        }
        AuthData auth = authDataAccess.getAuthInfoByToken(authToken);
        return userDataAccess.getUser(auth.username());
    }

    // Takes username and returns user data associated with that username
    public UserData getUserOnUserName(String username) throws Exception {
        if (username == null) {
            throw new ServiceException(500, "Error: Username passed into getUserOnUserName is null");
        }
        return userDataAccess.getUser(username);
    }

    // Takes username and returns authorization data associated with that username
    public AuthData getAuthInfoByUsername(String username) throws Exception {
        if (username == null) {
            throw new ServiceException(500, "Error: Username passed to getAuthInfoByUsername is null");
        }
        return authDataAccess.getAuthInfoByUsername(username);
    }


    // ------ HELPER FUNCTIONS FOR SERVICE TESTS ------ //
    public UserDataAccess getUserDataAccess() {
        return userDataAccess;
    }

    public AuthDataAccess getAuthDataAccess() {
        return authDataAccess;
    }

}
