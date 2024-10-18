package dataaccess;

import model.AuthData;

public interface AuthDataAccess {
    AuthData getAuthInfo(String username);
    void addAuthToken(String username) throws Exception;
}
