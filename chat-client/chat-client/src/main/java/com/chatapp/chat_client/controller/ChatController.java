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
    @FXML private Button logoutButton;

    private final GroupService groupService = new GroupService();
    private final WebSocketService webSocketService = new WebSocketService();
    private final ObservableList<Node> messages = FXCollections.observableArrayList();
    private GroupDto currentGroup;
    private ContextMenu contextMenu;

    @FXML
    public void initialize() {
        messageListView.setItems(messages);
        connectAndCheckStatus();
        setupGroupSelectionListener();
        // The onAction is now handled in the FXML, so this line is not needed
        // sendButton.setOnAction(event -> handleSendMessage());
        setupContextMenu();
    }

    public void initData() {
        loadUserGroups();
    }

    private void setupContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem leaveGroupItem = new MenuItem("Leave Group");
        MenuItem addMemberItem = new MenuItem("Add Member");
        MenuItem deleteGroupItem = new MenuItem("Delete Group (Owner)");

        leaveGroupItem.setOnAction(event -> handleLeaveGroup());
        addMemberItem.setOnAction(event -> handleAddMember());
        deleteGroupItem.setOnAction(event -> handleDeleteGroup());

        contextMenu.getItems().addAll(deleteGroupItem, addMemberItem, new SeparatorMenuItem(), leaveGroupItem);

        userListView.setOnMouseClicked(event -> {
            contextMenu.hide();

            if (event.getButton() == MouseButton.SECONDARY) {
                GroupDto selectedGroup = userListView.getSelectionModel().getSelectedItem();

                if (selectedGroup != null && selectedGroup.memberUsernames().size() > 2) {
                    boolean isOwner = selectedGroup.ownerId().equals(getCurrentUserId());

                    deleteGroupItem.setVisible(isOwner);
                    addMemberItem.setVisible(isOwner);
                    leaveGroupItem.setVisible(!isOwner);

                    contextMenu.show(userListView, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    @FXML
    private void handleSendMessage() {
        String content = messageTextField.getText();
        if (content.isEmpty() || currentGroup == null) { return; }

        Long senderId = UserSession.getInstance().getUserId();
        String senderUsername = UserSession.getInstance().getUsername();
        if (senderId == null || senderUsername == null) return;

        ChatMessageDto.ChatMessageResponse messageForDisplay = new ChatMessageDto.ChatMessageResponse(
                currentGroup.id(), content, senderUsername, Instant.now()
        );
        messages.add(createMessageNode(messageForDisplay));
        messageListView.scrollTo(messages.size() - 1);

        ChatMessageDto.ChatMessageRequest messageForServer = new ChatMessageDto.ChatMessageRequest(
                currentGroup.id(), senderId, content
        );
        webSocketService.sendMessage("/app/chat.sendMessage", messageForServer);
        messageTextField.clear();
    }

    @FXML
    private void handleLogoutAction() {
        UserSession.getInstance().cleanUserSession();
        webSocketService.disconnect();
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        currentStage.close();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/LoginView.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("Chat Login");
            loginStage.setScene(new Scene(fxmlLoader.load(), 400, 350));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void handleDeleteGroup() {
        GroupDto selectedGroup = userListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Group");
        alert.setHeaderText("Are you sure you want to permanently delete '" + selectedGroup.toString() + "'?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    groupService.deleteGroup(selectedGroup.id(), getCurrentUserId());
                    Platform.runLater(() -> userListView.getItems().remove(selectedGroup));
                } catch (Exception e) {
                    Platform.runLater(() -> showErrorAlert("Error", "Failed to delete the group."));
                }
            }).start();
        }
    }

    private void handleLeaveGroup() {
        GroupDto selectedGroup = userListView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Group");
        alert.setHeaderText("Are you sure you want to leave the group '" + selectedGroup.toString() + "'?");
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

    private Node createMessageNode(ChatMessageDto.ChatMessageResponse message) {
        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        String senderName = message.senderUsername();
        if (currentGroup != null && currentGroup.memberUsernames().size() > 2 && !senderName.equals(UserSession.getInstance().getUsername())) {
            messageLabel.setText(senderName + ":\n" + message.content());
        } else {
            messageLabel.setText(message.content());
        }
        HBox messageContainer = new HBox(messageLabel);
        if (senderName.equals(UserSession.getInstance().getUsername())) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }
        return messageContainer;
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