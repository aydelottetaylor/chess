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
        try {
            if(newUser.username() == null || newUser.password() == null || newUser.email() == null) {
                throw new ServiceException(400, "bad request");
            }

            if (userDataAccess.getUser(newUser.username()) != null) {
                throw new ServiceException(403, "already taken");
            } else {
                userDataAccess.addUser(newUser);
                authDataAccess.addAuthToken(newUser.username());
            }
        } catch (Exception e) {
            throw new ServiceException(500, e.getMessage());
        }


        return authDataAccess.getAuthInfo(newUser.username());
    }

    public AuthData loginUser(UserData userInfo) throws Exception{
        try {
            UserData user = userDataAccess.getUser(userInfo.username());
            if (user == null) {
                throw new ServiceException(500, "no user found with username: "+userInfo.username());
            }
            if (Objects.equals(user.password(), userInfo.password())) {
                authDataAccess.addAuthToken(userInfo.username());
            } else {
                throw new ServiceException(401, "unauthorized");
            }
        } catch (Exception e) {
            throw new ServiceException(500, e.getMessage());
        }

        return authDataAccess.getAuthInfo(userInfo.username());
    }


}
