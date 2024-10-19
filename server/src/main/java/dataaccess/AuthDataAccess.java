package dataaccess;

import model.AuthData;

public interface AuthDataAccess {
    AuthData getAuthInfoByUsername(String username);
    void addAuthToken(String username) throws Exception;
    AuthData getAuthInfoByToken(String token);
    void removeAuthorization(AuthData authData) throws Exception;

}
