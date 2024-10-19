package service;

import dataaccess.*;
import model.*;

import java.util.Objects;

public class UserService {
    private final UserDataAccess userDataAccess;
    private final AuthDataAccess authDataAccess;

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
                authDataAccess.addAuthToken(newUser.username());
            }

        return authDataAccess.getAuthInfoByUsername(newUser.username());
    }

    public AuthData loginUser(UserData userInfo) throws Exception {
        UserData user = userDataAccess.getUser(userInfo.username());
        AuthData auth = authDataAccess.getAuthInfoByUsername(userInfo.username());
        if (user == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        if (Objects.equals(user.password(), userInfo.password())) {
            authDataAccess.addAuthToken(userInfo.username());
            return authDataAccess.getAuthInfoByUsername(userInfo.username());
        } else {
            throw new ServiceException(401, "Error: unauthorized");
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

    public UserData getUserOnAuthToken(String authToken) throws Exception {
        AuthData auth = authDataAccess.getAuthInfoByToken(authToken);
        return userDataAccess.getUser(auth.username());
    }

}
