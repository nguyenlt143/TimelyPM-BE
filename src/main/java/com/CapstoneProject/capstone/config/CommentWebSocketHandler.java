package com.CapstoneProject.capstone.config;

import com.CapstoneProject.capstone.dto.request.comment.CreateCommentRequest;
import com.CapstoneProject.capstone.dto.response.comment.CreateCommentResponse;
import com.CapstoneProject.capstone.model.User;
import com.CapstoneProject.capstone.repository.UserRepository;
import com.CapstoneProject.capstone.service.ICommentService;
import com.CapstoneProject.capstone.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ICommentService commentService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = session.getHandshakeHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "").trim();
            try {
                String username = jwtService.extractUserName(token);
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "", Collections.emptyList());
                if (jwtService.isTokenValid(token, userDetails)) {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    session.getAttributes().put("username", username);
                    session.getAttributes().put("userId", user.getId());
                    sessions.add(session);
                    log.info("Client connected: {} with username: {}", session.getId(), username);
                } else {
                    session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
                    log.warn("Invalid token for session: {}", session.getId());
                }
            } catch (Exception e) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token validation failed"));
                log.error("Token validation error for session: {}", session.getId(), e);
            }
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("No token provided in header"));
            log.warn("No token provided for session: {}", session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, String> data = objectMapper.readValue(message.getPayload(), Map.class);
            String questionId = data.get("questionId");
            String content = data.get("content");

            if (questionId == null || content == null || content.trim().isEmpty()) {
                session.sendMessage(new TextMessage("{\"error\": \"questionId and content are required\"}"));
                return;
            }

            UUID userId = (UUID) session.getAttributes().get("userId");
            if (userId == null) {
                session.sendMessage(new TextMessage("{\"error\": \"User not authenticated\"}"));
                return;
            }

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    (String) session.getAttributes().get("username"), "", Collections.emptyList());
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            user, null, userDetails.getAuthorities()));

            CreateCommentRequest request = new CreateCommentRequest();
            request.setContent(content);
            CreateCommentResponse response = commentService.createComment(UUID.fromString(questionId), request);

            broadcastComment(response);
            log.info("Broadcasted comment: {}", response.getContent());
        } catch (Exception e) {
            log.error("Error handling message from session {}: {}", session.getId(), e.getMessage());
            session.sendMessage(new TextMessage("{\"error\": \"Failed to process comment: " + e.getMessage() + "\"}"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("Client disconnected: {} with status: {}", session.getId(), status);
    }

    public void broadcastComment(CreateCommentResponse comment) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(comment);
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                    } catch (IOException e) {
                        log.error("Failed to send to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            }
        }
    }
}