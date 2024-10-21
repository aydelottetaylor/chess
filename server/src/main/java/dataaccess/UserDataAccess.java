package dataaccess;

import model.UserData;

import java.util.Collection;

public interface UserDataAccess {
    UserData getUser(String userName);
    void addUser(UserData user);
    void clearUsers();
    Collection<UserData> listUsers();

}
