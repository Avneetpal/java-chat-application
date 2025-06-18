package com.chatapp.chat_client.service;

import com.chatapp.chat_client.model.ChatMessageDto;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class WebSocketService {

    private StompSession stompSession;
    private String connectionError = null; // ADDED: To store any connection error

    public void connect() {
        if (stompSession != null && stompSession.isConnected()) {
            return;
        }
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String url = "ws://localhost:8080/ws";
        try {
            stompSession = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get();
            System.out.println("WebSocket connection successful!");
        } catch (InterruptedException | ExecutionException e) {
            // Store the error message and print the full stack trace
            this.connectionError = e.getMessage();
            e.printStackTrace();
            stompSession = null;
        }
    }

    public boolean isConnected() {
        return stompSession != null && stompSession.isConnected();
    }

    // ADDED: A new method to get the error message
    public String getConnectionError() {
        return connectionError;
    }

    public void subscribeToGroup(String topic, Consumer<ChatMessageDto.ChatMessageResponse> messageHandler) {
        if (!isConnected()) { return; }
        stompSession.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.ChatMessageResponse.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageHandler.accept((ChatMessageDto.ChatMessageResponse) payload);
            }
        });
    }

    public void sendMessage(String destination, ChatMessageDto.ChatMessageRequest message) {
        if (!isConnected()) { return; }
        stompSession.send(destination, message);
    }
}