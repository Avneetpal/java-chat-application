package com.chatapp.chat_client.service;

import com.chatapp.chat_client.model.GroupDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class GroupService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String baseUrl = "http://localhost:8080/api/groups";

    public List<GroupDto> getUserGroups() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse the JSON array into a List of GroupDto objects
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } else {
            throw new RuntimeException("Failed to fetch groups");
        }
    }
}