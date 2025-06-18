package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.MainApplication;
import com.chatapp.chat_client.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button registerButton;
    @FXML private Label infoLabel;
    @FXML private Hyperlink backToLoginLink;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleRegisterButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        new Thread(() -> {
            try {
                String response = authService.register(username, password);
                Platform.runLater(() -> {
                    infoLabel.setText(response);
                    // Make success message green
                    if (response.toLowerCase().contains("success")) {
                        infoLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                    } else {
                        infoLabel.setTextFill(javafx.scene.paint.Color.RED);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> infoLabel.setText("Error: Could not connect to server."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleBackToLoginClick() throws IOException {
        // This method takes the user back to the login screen
        Stage stage = (Stage) backToLoginLink.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/LoginView.fxml"));
        stage.setScene(new Scene(fxmlLoader.load(), 400, 300));
    }
}