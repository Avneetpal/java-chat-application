package com.chatapp.chat_client.service;

import com.chatapp.chat_client.model.ChatMessageDto;
import com.chatapp.chat_client.model.GroupDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class GroupService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String baseUrl = "http://localhost:8080/api/groups";

    public List<GroupDto> getUserGroups(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/user/" + userId))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } else {
            throw new RuntimeException("Failed to fetch groups");
        }
    }

    /**
     * UPDATED: The method signature now correctly accepts both user IDs.
     */
    public GroupDto startDirectMessage(Long currentUserId, Long targetUserId) throws Exception {
        // Create the JSON body with both IDs
        Map<String, Long> requestBody = Map.of(
                "currentUserId", currentUserId,
                "targetUserId", targetUserId
        );
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // Build the POST request to the /dm endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/dm"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), GroupDto.class);
        } else {
            throw new RuntimeException("Failed to start direct message. Server responded with: " + response.body());
        }
    }

    public List<ChatMessageDto.ChatMessageResponse> getMessageHistory(Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + groupId + "/messages"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } else {
            throw new RuntimeException("Failed to fetch message history");
        }
    }
}