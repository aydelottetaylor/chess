package dataaccess;

import model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

public class MySQL_UserDAO implements UserDataAccess {

    // Creates database and all tables
    public static void createDatabase() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
    }

    // Gets user on username
    public UserData getUser(String username) throws Exception {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users WHERE username =?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read user data: %s", e.getMessage()));
        }
        return null;
    }

    // Reads user data from statement and returns in UserData format
    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }

    // List all users
    public Collection<UserData> listUsers() throws Exception {
        var result = new ArrayList<UserData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readUser(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read user data: %s", e.getMessage()));
        }
        return result;
    }

    // Add user
    public void addUser(UserData user, String password) throws Exception {
        try {

            var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            DatabaseManager.executeUpdate(statement, user.username(), password, user.email());
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    // Clear users
    public void clearUsers() throws Exception {
        try {
            var statement = "TRUNCATE TABLE users";
            DatabaseManager.executeUpdate(statement);
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }





}