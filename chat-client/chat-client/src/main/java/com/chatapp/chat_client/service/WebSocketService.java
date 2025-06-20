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
import java.util.function.Consumer;

public class WebSocketService {

    private static final String URL = "ws://localhost:8080/ws";
    private StompSession session;
    private String connectionError;

    public void connect() {
        try {
            WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            session = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {}).get();
            System.out.println("WebSocket connection successful!");
        } catch (Exception e) {
            connectionError = "Could not connect to the server: " + e.getMessage();
            System.err.println(connectionError);
        }
    }

    public void subscribeToGroup(String topic, Consumer<ChatMessageDto.ChatMessageResponse> messageHandler) {
        if (session != null && session.isConnected()) {
            session.subscribe(topic, new StompFrameHandler() {
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
    }

    public void sendMessage(String destination, Object payload) {
        if (session != null && session.isConnected()) {
            session.send(destination, payload);
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public String getConnectionError() {
        return connectionError;
    }

    /**
     * ADDED: This method handles cleanly disconnecting from the WebSocket session.
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("WebSocket disconnected.");
        }
    }
}