package dataaccess;

import model.AuthData;

import java.util.Collection;

public interface AuthDataAccess {
    AuthData getAuthInfoByUsername(String username) throws Exception;
    void addAuthToken(String username, String authToken) throws Exception;
    AuthData getAuthInfoByToken(String token) throws Exception;
    void removeAuthorization(String token) throws Exception;
    void clearAuths();
    Collection<AuthData> getAllAuths() throws Exception;

}
