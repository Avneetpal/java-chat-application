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
    @FXML private Hyperlink registerLink;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");

        new Thread(() -> {
            try {
                String responseBody = authService.login(username, password);

                // --- ADDED DEBUG LINE 1 ---
                // This will show us exactly what the server sends back after a successful login.
                System.out.println("DEBUG: Server Login Response Body: " + responseBody);

                Platform.runLater(() -> {
                    if (responseBody != null) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode responseJson = objectMapper.readTree(responseBody);

                            long userId = responseJson.get("id").asLong();

                            UserSession.getInstance().setLoggedInUser(userId, username);

                            // --- ADDED DEBUG LINE 2 ---
                            // This will show us what ID and username the client thinks is logged in.
                            System.out.println("DEBUG: UserSession has been set with ID: " + UserSession.getInstance().getUserId() + ", Username: " + UserSession.getInstance().getUsername());

                            navigateToChatView();
                        } catch (IOException e) {
                            errorLabel.setText("Error: Failed to process server response.");
                            e.printStackTrace();
                        }
                    } else {
                        errorLabel.setText("Login failed. Please check credentials.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Error: Could not connect to the server."));
                e.printStackTrace();
            }
        }).start();
    }

    private void navigateToChatView() {
        try {
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/ChatView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);

            ChatController chatController = fxmlLoader.getController();
            chatController.initData();

            Stage chatStage = new Stage();
            chatStage.setTitle("Chat Application");
            chatStage.setScene(scene);
            chatStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleRegisterLinkClick() {
        try {
            Stage stage = (Stage) registerLink.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/RegisterView.fxml"));
            stage.setScene(new Scene(fxmlLoader.load(), 400, 350));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}