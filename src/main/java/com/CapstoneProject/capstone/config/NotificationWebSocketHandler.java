package com.CapstoneProject.capstone.config;

import com.CapstoneProject.capstone.dto.response.notification.GetNotificationResponse;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.CapstoneProject.capstone.repository.UserRepository;
import com.CapstoneProject.capstone.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("Client connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, String> data = objectMapper.readValue(message.getPayload(), Map.class);

            String token = data.get("token");
            if (token == null || token.trim().isEmpty()) {
                session.sendMessage(new TextMessage("{\"error\": \"Authentication token required\"}"));
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("No token provided"));
                return;
            }

            String username = jwtService.extractUserName(token);
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "", Collections.emptyList());
            if (!jwtService.isTokenValid(token, userDetails)) {
                session.sendMessage(new TextMessage("{\"error\": \"Invalid token\"}"));
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
                return;
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            session.getAttributes().put("username", username);
            session.getAttributes().put("userId", user.getId());
            log.info("Client authenticated via message: {} with userId: {}", session.getId(), user.getId());
//            session.sendMessage(new TextMessage("{\"message\": \"Authenticated\"}"));
        } catch (Exception e) {
            log.error("Error during authentication for session {}: {}", session.getId(), e.getMessage());
            session.sendMessage(new TextMessage("{\"error\": \"Authentication failed: " + e.getMessage() + "\"}"));
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("Client disconnected: {} with status: {}", session.getId(), status);
    }

    public void broadcastNotification(GetNotificationResponse notification) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(notification);
        synchronized (sessions) {
            log.info("Broadcasting to {} sessions for userId: {}", sessions.size(), notification.getUserId());
            for (WebSocketSession session : sessions) {
                UUID sessionUserId = (UUID) session.getAttributes().get("userId");
                if (session.isOpen() && sessionUserId != null && sessionUserId.equals(notification.getUserId())) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                        log.info("Sent to session {} for userId: {}", session.getId(), notification.getUserId());
                    } catch (IOException e) {
                        log.error("Failed to send to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            }
        }
    }
}
