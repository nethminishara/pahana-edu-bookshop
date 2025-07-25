package com.pahanaedu.service;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationService {
    private UserDAO userDAO;
    
    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }
    
    public User authenticateUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        return userDAO.authenticateUser(username, hashedPassword);
    }
    
    public boolean registerUser(String username, String password, String role) {
        String hashedPassword = hashPassword(password);
        User user = new User(username, hashedPassword, role);
        return userDAO.addUser(user);
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
