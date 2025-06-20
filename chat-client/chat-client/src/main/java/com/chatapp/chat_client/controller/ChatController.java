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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ChatController {

    @FXML private ListView<GroupDto> userListView;
    @FXML private ListView<Node> messageListView;
    @FXML private TextField messageTextField;
    @FXML private Button sendButton;
    @FXML private Button newChatButton;
    @FXML private Button createGroupButton;

    private final GroupService groupService = new GroupService();
    private final WebSocketService webSocketService = new WebSocketService();
    private final ObservableList<Node> messages = FXCollections.observableArrayList();
    private GroupDto currentGroup;
    private ContextMenu groupContextMenu;

    @FXML
    public void initialize() {
        messageListView.setItems(messages);
        connectAndCheckStatus();
        setupGroupSelectionListener();
        sendButton.setOnAction(event -> handleSendMessage());
        setupContextMenu();
    }

    /**
     * UPDATED: This method now creates a simple, elegant text layout.
     * It relies on alignment, not background colors, to distinguish messages.
     */
    private Node createMessageNode(ChatMessageDto.ChatMessageResponse message) {
        // The Label will hold our message text.
        Label messageLabel = new Label();
        // We apply a CSS class to it to control the font style.
        messageLabel.getStyleClass().add("message-label");

        String senderName = message.senderUsername();

        // For group chats with 3+ members, show the sender's name above their message.
        if (currentGroup != null && currentGroup.memberUsernames().size() > 2 && !senderName.equals(UserSession.getInstance().getUsername())) {
            messageLabel.setText(senderName + ":\n" + message.content());
        } else {
            // For one-on-one chats, just show the content.
            messageLabel.setText(message.content());
        }

        // We place the label inside an HBox to control its alignment.
        HBox messageContainer = new HBox(messageLabel);

        // Your messages align to the right. Received messages align to the left.
        if (senderName.equals(UserSession.getInstance().getUsername())) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }

        return messageContainer;
    }

    @FXML
    private void handleSendMessage() {
        String content = messageTextField.getText();
        if (content.isEmpty() || currentGroup == null) { return; }

        Long senderId = UserSession.getInstance().getUserId();
        String senderUsername = UserSession.getInstance().getUsername();
        if (senderId == null || senderUsername == null) return;

        // 1. Create DTO for immediate display
        ChatMessageDto.ChatMessageResponse messageForDisplay = new ChatMessageDto.ChatMessageResponse(
                currentGroup.id(), content, senderUsername, Instant.now()
        );
        // 2. Add to our own screen right away
        messages.add(createMessageNode(messageForDisplay));
        messageListView.scrollTo(messages.size() - 1);

        // 3. Create DTO to send to server
        ChatMessageDto.ChatMessageRequest messageForServer = new ChatMessageDto.ChatMessageRequest(
                currentGroup.id(), senderId, content
        );
        // 4. Send to server
        webSocketService.sendMessage("/app/chat.sendMessage", messageForServer);

        messageTextField.clear();
    }

    private void onMessageReceived(ChatMessageDto.ChatMessageResponse message) {
        Platform.runLater(() -> {
            if (currentGroup != null && message.groupId().equals(currentGroup.id())) {
                if (!message.senderUsername().equals(UserSession.getInstance().getUsername())) {
                    messages.add(createMessageNode(message));
                    messageListView.scrollTo(messages.size() - 1);
                }
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
                        messages.add(createMessageNode(msg));
                    }
                    if (!messages.isEmpty()) {
                        messageListView.scrollTo(messages.size() - 1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- All other methods below are for setup and remain unchanged ---

    private void setupContextMenu() {
        groupContextMenu = new ContextMenu();
        MenuItem leaveGroupItem = new MenuItem("Leave Group");
        leaveGroupItem.setOnAction(event -> handleLeaveGroup());
        MenuItem addMemberItem = new MenuItem("Add Member");
        addMemberItem.setOnAction(event -> handleAddMember());
        groupContextMenu.getItems().addAll(leaveGroupItem, addMemberItem);
        userListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                GroupDto selectedGroup = userListView.getSelectionModel().getSelectedItem();
                if (selectedGroup != null && selectedGroup.memberUsernames().size() > 2) {
                    groupContextMenu.show(userListView, event.getScreenX(), event.getScreenY());
                } else {
                    groupContextMenu.hide();
                }
            } else {
                groupContextMenu.hide();
            }
        });
    }

    private void handleLeaveGroup() {
        GroupDto selectedGroup = userListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Group");
        alert.setHeaderText("Are you sure you want to leave the group '" + selectedGroup.toString() + "'?");
        alert.setContentText("If you are the last member, the group will be deleted.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            leaveSelectedGroup(selectedGroup);
        }
    }

    private void handleAddMember() {
        GroupDto selectedGroup = userListView.getSelectionModel().getSelectedItem();
        if (selectedGroup != null) {
            openAddMemberDialog(selectedGroup);
        }
    }

    private void openAddMemberDialog(GroupDto group) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/AddMemberView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add Members");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(fxmlLoader.load(), 350, 400));
            AddMemberController addMemberController = fxmlLoader.getController();
            addMemberController.initData(group);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("UI Error", "Could not open the Add Member window.");
        }
    }

    private void leaveSelectedGroup(GroupDto groupToLeave) {
        new Thread(() -> {
            try {
                groupService.leaveGroup(groupToLeave.id(), getCurrentUserId());
                Platform.runLater(() -> userListView.getItems().remove(groupToLeave));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Error", "Failed to leave the group."));
            }
        }).start();
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
        }
    }

    @FXML
    private void handleCreateGroupAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/CreateGroupView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Create New Group");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(fxmlLoader.load(), 350, 400));
            CreateGroupController createGroupController = fxmlLoader.getController();
            createGroupController.setParentController(this);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectAndCheckStatus() {
        new Thread(() -> {
            webSocketService.connect();
        }).start();
    }

    public Long getCurrentUserId() {
        return UserSession.getInstance().getUserId();
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