package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.sql.*;

public class MySQL_UserDAO implements UserDataAccess {

    public UserData getUser(String username) {
        return new UserData("", "", "");
    }

    // List all users
    public Collection<UserData> listUsers() {
        return new ArrayList<UserData>();
    }

    // Add user or update user to HashMap
    public void addUser(UserData user) {

    }

    // Clear HashMap of users
    public void clearUsers() {

    }



}