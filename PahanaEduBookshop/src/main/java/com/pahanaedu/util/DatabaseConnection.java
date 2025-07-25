package com.pahanaedu.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/pahana_edu_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; 
    
    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connection established successfully!");
        } catch (ClassNotFoundException ex) {
            System.err.println("MySQL Driver not found: " + ex.getMessage());
            throw new SQLException("Database Connection Creation Failed : " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("Database connection failed: " + ex.getMessage());
            throw ex;
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else if (instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public static void testConnection() {
        try {
            DatabaseConnection db = getInstance();
            if (db.getConnection() != null && !db.getConnection().isClosed()) {
                System.out.println("Database connection test: SUCCESS");
            } else {
                System.out.println("Database connection test: FAILED");
            }
        } catch (SQLException e) {
            System.err.println("Database connection test: FAILED - " + e.getMessage());
        }
    }
}
