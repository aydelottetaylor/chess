package dataaccess;

import model.UserData;

public interface UserDataAccess {
    UserData getUser(String userName);
    void addUser(UserData user);


}
