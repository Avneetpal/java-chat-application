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

    public boolean login(String username, String password) throws Exception {
        // Create the JSON data to send to the server
        Map<String, String> requestBody = Map.of("username", username, "password", password);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // Build the HTTP POST request with the URL and the JSON body
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send the request and get the server's response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // The login is successful if the server responds with a 200 OK status code
        return response.statusCode() == 200;
    }
}