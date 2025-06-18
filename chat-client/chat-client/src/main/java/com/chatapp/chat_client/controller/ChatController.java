package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.model.ChatMessageDto;
import com.chatapp.chat_client.model.GroupDto;
import com.chatapp.chat_client.service.GroupService;
import com.chatapp.chat_client.service.WebSocketService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import java.util.List;

public class ChatController {

    @FXML private ListView<GroupDto> userListView;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageTextField;
    @FXML private Button sendButton;

    private final GroupService groupService = new GroupService();
    private final WebSocketService webSocketService = new WebSocketService();
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private GroupDto currentGroup;

    @FXML
    public void initialize() {
        messageListView.setItems(messages);

        // Try to connect and then check for errors
        connectAndCheckStatus();

        loadUserGroups();
        setupGroupSelectionListener();
        sendButton.setOnAction(event -> handleSendMessage());
    }

    private void connectAndCheckStatus() {
        new Thread(() -> {
            webSocketService.connect();
            // After attempting to connect, check the status on the UI thread
            Platform.runLater(() -> {
                if (!webSocketService.isConnected()) {
                    showErrorAlert("WebSocket Connection Failed", webSocketService.getConnectionError());
                }
            });
        }).start();
    }

    private void setupGroupSelectionListener() {
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentGroup = newSelection;
                messages.clear();
                String topic = "/topic/group/" + currentGroup.id();
                webSocketService.subscribeToGroup(topic, this::onMessageReceived);
            }
        });
    }

    private void handleSendMessage() {
        String content = messageTextField.getText();
        if (content.isEmpty() || currentGroup == null) { return; }

        if (!webSocketService.isConnected()) {
            showErrorAlert("Connection Error", "Not connected to server. Please restart.");
            return;
        }
        long senderId = 1L; // Placeholder
        ChatMessageDto.ChatMessageRequest message = new ChatMessageDto.ChatMessageRequest(currentGroup.id(), senderId, content);
        webSocketService.sendMessage("/app/chat.sendMessage", message);
        messageTextField.clear();
    }

    private void onMessageReceived(ChatMessageDto.ChatMessageResponse message) {
        Platform.runLater(() -> {
            String formattedMessage = message.senderUsername() + ": " + message.content();
            messages.add(formattedMessage);
        });
    }

    private void loadUserGroups() {
        new Thread(() -> {
            try {
                List<GroupDto> groups = groupService.getUserGroups();
                Platform.runLater(() -> userListView.setItems(FXCollections.observableArrayList(groups)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}