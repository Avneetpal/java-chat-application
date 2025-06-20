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
        setupGroupSelectionListener();
        sendButton.setOnAction(event -> handleSendMessage());
    }

    public void initData() {
        loadUserGroups();
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
                        String formattedMessage = (msg.senderUsername() != null ? msg.senderUsername() : "Unknown") + ": " + msg.content();
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
            String formattedMessage = (message.senderUsername() != null ? message.senderUsername() : "Unknown") + ": " + message.content();
            messages.add(formattedMessage);
        });
    }

    public void loadUserGroups() {
        new Thread(() -> {
            try {
                Long userId = UserSession.getInstance().getUserId();

                // --- ADDED DEBUG LINE 3 ---
                // This will show us which user's chats this window is trying to load.
                System.out.println("DEBUG: ChatController is now fetching groups for userId: " + userId);

                if (userId != null) {
                    final List<GroupDto> groups = groupService.getUserGroups(userId);
                    Platform.runLater(() -> userListView.setItems(FXCollections.observableArrayList(groups)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void addNewGroupAndSelect(GroupDto newGroup) {
        if (!userListView.getItems().contains(newGroup)) {
            userListView.getItems().add(newGroup);
        }
        userListView.getSelectionModel().select(newGroup);
        userListView.scrollTo(newGroup);
        userListView.requestFocus();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}