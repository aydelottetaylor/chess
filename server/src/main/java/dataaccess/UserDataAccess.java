package dataaccess;

import model.UserData;

import java.util.Collection;

public interface UserDataAccess {
    UserData getUser(String userName) throws Exception ;
    void addUser(UserData user, String password) throws Exception;
    void clearUsers() throws Exception;
    Collection<UserData> listUsers() throws Exception;

}
