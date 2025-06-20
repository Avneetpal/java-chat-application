package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.model.UserDto; // <-- The required import
import com.chatapp.chat_client.service.GroupService;
import com.chatapp.chat_client.service.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List; // <-- Ensure this is also imported
import java.util.Set;
import java.util.stream.Collectors;

public class CreateGroupController {

    @FXML private TextField groupNameField;
    @FXML private ListView<UserDto> usersListView;
    @FXML private Button createGroupButton;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();
    private final GroupService groupService = new GroupService();
    private ChatController parentController;

    public void setParentController(ChatController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        loadAllUsers();
    }

    private void loadAllUsers() {
        new Thread(() -> {
            try {
                final List<UserDto> users = userService.getAllUsers();
                Platform.runLater(() -> usersListView.setItems(FXCollections.observableArrayList(users)));
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Failed to load users."));
            }
        }).start();
    }

    @FXML
    private void handleCreateGroupAction() {
        String groupName = groupNameField.getText();
        if (groupName == null || groupName.trim().isEmpty()) {
            errorLabel.setText("Group name cannot be empty.");
            return;
        }
        ObservableList<UserDto> selectedUsers = usersListView.getSelectionModel().getSelectedItems();
        if (selectedUsers.isEmpty()) {
            errorLabel.setText("You must select at least one member.");
            return;
        }
        Set<Long> memberIds = selectedUsers.stream().map(UserDto::id).collect(Collectors.toSet());
        memberIds.add(parentController.getCurrentUserId());

        new Thread(() -> {
            try {
                groupService.createGroup(groupName, memberIds);
                Platform.runLater(() -> {
                    parentController.loadUserGroups();
                    closeWindow();
                });
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Failed to create group."));
            }
        }).start();
    }

    private void closeWindow() {
        ((Stage) createGroupButton.getScene().getWindow()).close();
    }
}