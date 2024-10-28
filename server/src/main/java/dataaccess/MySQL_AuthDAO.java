package dataaccess;

import model.AuthData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySQL_AuthDAO implements AuthDataAccess {

    // Get Auth Info by Username
    public AuthData getAuthInfoByUsername(String username) throws Exception {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auths WHERE username=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read auth data: %s", e.getMessage()));
        }
        return null;
    }

    // Read auth info from statement and return in AuthData format
    private AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var authToken = rs.getString("token");
        return new AuthData(username, authToken);
    }

    // Get auth info from token
    public AuthData getAuthInfoByToken(String token) throws Exception {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auths WHERE token=?";
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                try(var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read auth data: %s", e.getMessage()));
        }
        return null;
    }

    // Add user auth information to database
    public void addAuthToken(String username, String authToken) throws Exception {
        try {
            var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
            DatabaseManager.executeUpdate(statement, username, authToken);
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    // Remove user auth information from database
    public void removeAuthorization(String token) throws Exception {
        try {
            var statement = "DELETE FROM auths WHERE token=?";
            DatabaseManager.executeUpdate(statement, token);
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    // Clear all auth information from database
    public void clearAuths() {
        try {
            var statement = "TRUNCATE TABLE auths";
            DatabaseManager.executeUpdate(statement);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Get all auth information from database
    public Collection<AuthData> getAllAuths() throws Exception {
        var result = new ArrayList<AuthData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auths";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readAuth(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read auth data: %s", e.getMessage()));
        }
        return result;
    }


}