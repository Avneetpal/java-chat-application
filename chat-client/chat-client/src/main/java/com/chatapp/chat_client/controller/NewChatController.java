package com.chatapp.chat_client.controller;

import com.chatapp.chat_client.model.GroupDto;
import com.chatapp.chat_client.model.UserDto;
import com.chatapp.chat_client.service.GroupService;
import com.chatapp.chat_client.service.UserSession;
import com.chatapp.chat_client.service.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewChatController {
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ListView<UserDto> resultsListView;

    private final UserService userService = new UserService();
    private final GroupService groupService = new GroupService();
    private ChatController parentController;

    public void setParentController(ChatController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        // Add a listener to handle clicks on the search results
        resultsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click to start chat
                UserDto selectedUser = resultsListView.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    startChatWithUser(selectedUser);
                }
            }
        });
    }

    @FXML
    private void handleSearchAction() {
        String query = searchField.getText();
        if (query.isEmpty()) return;

        new Thread(() -> {
            try {
                var users = userService.searchUsers(query);
                Platform.runLater(() -> resultsListView.setItems(FXCollections.observableArrayList(users)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * UPDATED: This method now calls the new 'selectAndFocusGroup' method on the parent controller.
     */
    // This is the UPDATED method in NewChatController.java
    private void startChatWithUser(UserDto user) {
        new Thread(() -> {
            try {
                Long currentUserId = UserSession.getInstance().getUserId();
                if(currentUserId == null) { return; }

                // 1. Call the service and get the newly created group DTO
                final GroupDto newGroup = groupService.startDirectMessage(currentUserId, user.id());

                Platform.runLater(() -> {
                    // 2. Call the new, simpler method on the parent controller
                    parentController.addNewGroupAndSelect(newGroup);

                    // 3. Close this pop-up window
                    ((Stage) searchButton.getScene().getWindow()).close();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}