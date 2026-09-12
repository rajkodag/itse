package com.nexdesk.service;

import com.nexdesk.db.DatabaseManager;
import com.nexdesk.model.User;

import java.sql.*;

public class UserService {
    private static final String SELECT =
            "SELECT USER_ID,FULL_NAME,EMAIL,USER_PASSWORD,USER_ROLE " +
            "FROM NX_USERS WHERE LOWER(EMAIL)=LOWER(?)";

    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        String sql = SELECT + " AND USER_PASSWORD=?";

        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, email.trim());
            p.setString(2, password);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    public User signup(String name, String email, String password) {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 4) {
            throw new IllegalArgumentException(
                    "Name, email and a password of at least 4 characters are required."
            );
        }

        String cleanName = name.trim();
        String cleanEmail = email.trim().toLowerCase();

        if (!cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        try (Connection c = DatabaseManager.getConnection()) {
            try (PreparedStatement check = c.prepareStatement(
                    "SELECT COUNT(*) FROM NX_USERS WHERE LOWER(EMAIL)=LOWER(?)")) {
                check.setString(1, cleanEmail);
                try (ResultSet r = check.executeQuery()) {
                    if (r.next() && r.getInt(1) > 0) {
                        throw new IllegalArgumentException(
                                "An account with this email already exists."
                        );
                    }
                }
            }

            String insert =
                    "INSERT INTO NX_USERS " +
                    "(USER_ID,FULL_NAME,EMAIL,USER_PASSWORD,USER_ROLE) " +
                    "VALUES(NX_USER_SEQ.NEXTVAL,?,?,?,'USER')";

            try (PreparedStatement p = c.prepareStatement(insert)) {
                p.setString(1, cleanName);
                p.setString(2, cleanEmail);
                p.setString(3, password);
                p.executeUpdate();
            }

            try (PreparedStatement q = c.prepareStatement(SELECT)) {
                q.setString(1, cleanEmail);
                try (ResultSet r = q.executeQuery()) {
                    if (r.next()) return map(r);
                }
            }

            throw new RuntimeException("Account was created but could not be read.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new IllegalArgumentException(
                        "An account with this email already exists."
                );
            }
            throw new RuntimeException("Sign-up failed: " + e.getMessage(), e);
        }
    }

    private User map(ResultSet r) throws SQLException {
        return new User(
                r.getInt("USER_ID"),
                r.getString("FULL_NAME"),
                r.getString("EMAIL"),
                r.getString("USER_PASSWORD"),
                r.getString("USER_ROLE")
        );
    }
}
