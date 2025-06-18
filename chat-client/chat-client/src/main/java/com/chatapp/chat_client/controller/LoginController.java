package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.MainApplication;
import com.chatapp.chat_client.service.AuthService;
import com.chatapp.chat_client.service.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink; // The fx:id for the hyperlink in the FXML

    private final AuthService authService = new AuthService();

    /**
     * This is the updated method for handling the login button click.
     * It now correctly handles the JSON response from the server.
     */
    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");

        new Thread(() -> {
            try {
                // AuthService.login() now returns a String (the JSON body) or null
                String responseBody = authService.login(username, password);

                Platform.runLater(() -> {
                    if (responseBody != null) {
                        try {
                            // Use ObjectMapper to parse the JSON string
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode responseJson = objectMapper.readTree(responseBody);
                            long userId = responseJson.get("userId").asLong();

                            // Save the logged-in user's info to our session manager
                            UserSession.getInstance().setLoggedInUser(userId, username);

                            System.out.println("Login Successful! Navigating to chat view...");
                            navigateToChatView();

                        } catch (IOException e) {
                            errorLabel.setText("Error: Failed to process server response.");
                            e.printStackTrace();
                        }
                    } else {
                        errorLabel.setText("Login failed. Please check your credentials.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Error: Could not connect to the server."));
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * This method handles the click on the "Register" hyperlink.
     */
    @FXML
    private void handleRegisterLinkClick() {
        try {
            // Get the current window (stage) to switch its content
            Stage stage = (Stage) errorLabel.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/RegisterView.fxml"));
            stage.setScene(new Scene(fxmlLoader.load(), 400, 350));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to navigate to the chat window.
     */
    private void navigateToChatView() {
        try {
            // Close the current login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

            // Load and show the new chat window
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/ChatView.fxml"));
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat");
            chatStage.setScene(new Scene(fxmlLoader.load(), 800, 600));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}