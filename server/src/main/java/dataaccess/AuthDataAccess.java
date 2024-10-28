package dataaccess;

import model.AuthData;

import java.util.Collection;

public interface AuthDataAccess {
    AuthData getAuthInfoByUsername(String username) throws Exception;
    void addAuthToken(String username, String authToken) throws Exception;
    AuthData getAuthInfoByToken(String token);
    void removeAuthorization(AuthData authData) throws Exception;
    void clearAuths() throws Exception;
    Collection<AuthData> getAllAuths();

}
