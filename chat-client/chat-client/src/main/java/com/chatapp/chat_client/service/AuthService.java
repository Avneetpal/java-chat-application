package com.chatapp.chat_client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "http://localhost:8080/api/auth";

    /**
     * Attempts to log in a user.
     * @param username The user's username.
     * @param password The user's password.
     * @return The JSON response body from the server on success (containing userId), or null on failure.
     * @throws Exception if the network call fails.
     */
    public String login(String username, String password) throws Exception {
        // Create the JSON body: {"username": "user", "password": "pwd"}
        Map<String, String> requestBody = Map.of("username", username, "password", password);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // Build the POST request to the /login endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // If the login was successful (status 200), return the JSON response body.
        // Otherwise, return null to indicate failure.
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            return null;
        }
    }

    /**
     * Attempts to register a new user.
     * @param username The desired username.
     * @param password The desired password.
     * @return The response message from the server (e.g., "User registered successfully!").
     * @throws Exception if the network call fails.
     */
    public String register(String username, String password) throws Exception {
        Map<String, String> requestBody = Map.of("username", username, "password", password);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Return the server's response message (e.g., "User registered successfully!" or "Username already taken")
        return response.body();
    }
}