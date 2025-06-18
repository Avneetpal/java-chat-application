package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.chatapp.chat_client.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    // These @FXML annotations link these variables to the components you defined in your FXML file.
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    // The controller needs the AuthService to talk to the backend.
    // Make sure you have already created the AuthService.java file.
    private final AuthService authService = new AuthService();

    /**
     * This is the method that your FXML file was looking for.
     * The @FXML annotation makes it visible to the FXML loader.
     * It will be executed every time the login button is clicked.
     */

    // This is the new version of the method in LoginController.java
    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText(""); // Clear previous errors

        // Run authentication in a background thread
        new Thread(() -> {
            try {
                boolean success = authService.login(username, password);

                // Update the UI on the main JavaFX thread
                Platform.runLater(() -> {
                    if (success) {
                        System.out.println("Login Successful! Navigating to chat view...");
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
}