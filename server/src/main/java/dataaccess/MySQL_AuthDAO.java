package dataaccess;

import model.AuthData;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;

public class MySQL_AuthDAO implements AuthDataAccess {

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

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var authToken = rs.getString("token");
        return new AuthData(username, authToken);
    }

    public AuthData getAuthInfoByToken(String token) {
        return new AuthData("", "");
    }

    public void addAuthToken(String username, String authToken) throws Exception {
        try {
            var statement = "INSERT INTO auths (username, token) VALUES (?, ?)";
            DatabaseManager.executeUpdate(statement, username, authToken);
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    public void removeAuthorization(AuthData authData) {

    }

    public void clearAuths() throws Exception {
        try {
            var statement = "TRUNCATE TABLE auths";
            DatabaseManager.executeUpdate(statement);
        } catch (Exception e) {
            throw new DataAccessException(500, e.getMessage());
        }
    }

    public Collection<AuthData> getAllAuths() {
        HashMap<String, AuthData> authsByToken = new HashMap<>();
        return authsByToken.values();
    }


}