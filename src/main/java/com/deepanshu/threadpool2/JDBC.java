package com.deepanshu.threadpool2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

/*
 * JDBC helper class
 * ------------------
 * Responsibility: central place for all database interaction (connections + simple queries).
 */


public class JDBC {
    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/registration";
    private static final String USER = "deepanshusharma";
    private static final String PASSWORD = "JAVA@20@25_";


    // Load MySQL JDBC drivers
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load JDBC driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a new Connection each call.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);

    }

    /**
     * Inserts one user row.
     */
    public static void insertUser(String username, String gmail, String hashedpassword) {
        final String sql = "INSERT INTO users(username,gmail,password) VALUES(?,?,?)";

        // try‑with‑resources ensures we always close resources
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Map Java variables → ? placeholders (1‑based index
            stmt.setString(1, username);
            stmt.setString(2, gmail);

            stmt.setString(3, hashedpassword);

            stmt.executeUpdate();
            System.out.println("✅ User inserted successfully!" + username);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if a row with matching username + password hash exists.
     */
    public static boolean validateUser(String username, String hashedPassword) {
        System.out.println("🔐 Login Attempt - Username: " + username);
        System.out.println("🔑 Entered Hash: " + hashedPassword);

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if user exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


    }
}
