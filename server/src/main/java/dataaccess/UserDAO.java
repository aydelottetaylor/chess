package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Collection;

public class UserDAO implements UserDataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();

    // Get a user from HashMap
    public UserData getUser(String username) {
        return users.get(username);
    }

    // List all users
    public Collection<UserData> listUsers() {
        return users.values();
    }

    // Add user or update user to HashMap
    public void addUser(UserData user) {
        users.put(user.username(), user);
    }

    // Delete a user from the HashMap
    public void removeUser(String username) {
        users.remove(username);
    }

    // Clear HashMap of users
    public void clearUsers() {
        users.clear();
    }

}
