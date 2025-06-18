package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.MainApplication;
import com.chatapp.chat_client.model.ChatMessageDto;
import com.chatapp.chat_client.model.GroupDto;
import com.chatapp.chat_client.service.GroupService;
import com.chatapp.chat_client.service.UserSession;
import com.chatapp.chat_client.service.WebSocketService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ChatController {

    @FXML private ListView<GroupDto> userListView;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageTextField;
    @FXML private Button sendButton;
    @FXML private Button newChatButton;

    private final GroupService groupService = new GroupService();
    private final WebSocketService webSocketService = new WebSocketService();
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private GroupDto currentGroup;

    @FXML
    public void initialize() {
        messageListView.setItems(messages);
        connectAndCheckStatus();
        loadUserGroups();
        setupGroupSelectionListener();
        sendButton.setOnAction(event -> handleSendMessage());
    }

    @FXML
    private void handleNewChatAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/NewChatView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Start New Chat");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(fxmlLoader.load(), 400, 500));
            NewChatController newChatController = fxmlLoader.getController();
            newChatController.setParentController(this);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("UI Error", "Could not load the new chat window.");
        }
    }

    private void connectAndCheckStatus() {
        new Thread(() -> {
            webSocketService.connect();
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
                fetchMessageHistory(currentGroup.id());
                String topic = "/topic/group/" + currentGroup.id();
                webSocketService.subscribeToGroup(topic, this::onMessageReceived);
            }
        });
    }

    private void fetchMessageHistory(Long groupId) {
        new Thread(() -> {
            try {
                final List<ChatMessageDto.ChatMessageResponse> history = groupService.getMessageHistory(groupId);
                Platform.runLater(() -> {
                    messages.clear();
                    for (ChatMessageDto.ChatMessageResponse msg : history) {
                        String formattedMessage = msg.senderUsername() + ": " + msg.content();
                        messages.add(formattedMessage);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleSendMessage() {
        String content = messageTextField.getText();
        if (content.isEmpty() || currentGroup == null) { return; }

        if (!webSocketService.isConnected()) {
            showErrorAlert("Connection Error", "Not connected to server. Please restart.");
            return;
        }
        Long senderId = UserSession.getInstance().getUserId();
        if (senderId == null) return;

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

    public void loadUserGroups() {
        new Thread(() -> {
            try {
                Long userId = UserSession.getInstance().getUserId();
                if (userId != null) {
                    final List<GroupDto> groups = groupService.getUserGroups(userId);
                    Platform.runLater(() -> userListView.setItems(FXCollections.observableArrayList(groups)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * ADDED: This new public method allows the NewChatController to select a group.
     */
    public void selectAndFocusGroup(GroupDto groupToSelect) {
        // Refresh the entire list first to ensure the new group is present
        loadUserGroups();

        // We use Platform.runLater to give the UI a moment to update from the background thread.
        Platform.runLater(() -> {
            // A small delay helps ensure the ListView has rendered its new items
            // before we try to select one.
            try {
                Thread.sleep(150); // wait 150 milliseconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            // Programmatically select the new group
            userListView.getSelectionModel().select(groupToSelect);
            userListView.scrollTo(groupToSelect);
            userListView.requestFocus();
        });
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}