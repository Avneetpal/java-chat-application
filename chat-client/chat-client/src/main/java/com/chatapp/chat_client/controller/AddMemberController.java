package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.model.GroupDto;
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

public class AddMemberController {

    @FXML private Label titleLabel;
    @FXML private ListView<UserDto> usersListView;
    @FXML private Button addMembersButton;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();
    private final GroupService groupService = new GroupService();
    private GroupDto currentGroup;

    public void initData(GroupDto group) {
        this.currentGroup = group;
        titleLabel.setText("Add Members to '" + group.toString() + "'");
        loadUsersNotInGroup();
    }

    @FXML
    public void initialize() {
        usersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadUsersNotInGroup() {
        new Thread(() -> {
            try {
                final List<UserDto> allUsers = userService.getAllUsers();
                Platform.runLater(() -> {
                    Set<String> currentMemberUsernames = currentGroup.memberUsernames();
                    List<UserDto> nonMembers = allUsers.stream()
                            .filter(user -> !currentMemberUsernames.contains(user.username()))
                            .collect(Collectors.toList());
                    usersListView.setItems(FXCollections.observableArrayList(nonMembers));
                });
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Failed to load users."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddMembersAction() {
        ObservableList<UserDto> selectedUsers = usersListView.getSelectionModel().getSelectedItems();
        if (selectedUsers.isEmpty()) {
            errorLabel.setText("Please select at least one user to add.");
            return;
        }
        Set<Long> memberIdsToAdd = selectedUsers.stream()
                .map(UserDto::id)
                .collect(Collectors.toSet());
        new Thread(() -> {
            try {
                groupService.addMembers(currentGroup.id(), memberIdsToAdd);
                Platform.runLater(this::closeWindow);
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Failed to add members."));
                e.printStackTrace();
            }
        }).start();
    }

    private void closeWindow() {
        ((Stage) addMembersButton.getScene().getWindow()).close();
    }
}