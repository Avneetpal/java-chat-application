package com.chatapp.chat_client.service;

// This class uses the Singleton pattern to hold the logged-in user's info
// so it can be accessed from anywhere in the application.
public class UserSession {

    private static UserSession instance;
    private Long userId;
    private String username;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setLoggedInUser(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void cleanUserSession() {
        userId = null;
        username = null;
    }
}