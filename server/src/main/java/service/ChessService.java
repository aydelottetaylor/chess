package service;

import dataaccess.*;
import model.*;

import java.util.Objects;

public class ChessService {
    private final UserDataAccess userDataAccess;
    private final AuthDataAccess authDataAccess;

    public ChessService() {
        this.userDataAccess = new UserDAO();
        this.authDataAccess = new AuthDAO();
    }

    public AuthData registerUser(UserData newUser) throws Exception {
            if(newUser.username() == null || newUser.password() == null || newUser.email() == null) {
                throw new ServiceException(400, "Error: Bad request");
            }

            if (userDataAccess.getUser(newUser.username()) != null) {
                throw new ServiceException(403, "Error: Forbidden");
            } else {
                userDataAccess.addUser(newUser);
                authDataAccess.addAuthToken(newUser.username());
            }

        return authDataAccess.getAuthInfo(newUser.username());
    }

    public AuthData loginUser(UserData userInfo) throws Exception {
        UserData user = userDataAccess.getUser(userInfo.username());
        if (user == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }
        if (Objects.equals(user.password(), userInfo.password())) {
            authDataAccess.addAuthToken(userInfo.username());
            return authDataAccess.getAuthInfo(userInfo.username());
        } else {
            throw new ServiceException(401, "Error: unauthorized");
        }
    }

}
