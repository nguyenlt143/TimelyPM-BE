package com.CapstoneProject.capstone.config;

import com.CapstoneProject.capstone.dto.request.comment.CreateCommentRequest;
import com.CapstoneProject.capstone.dto.response.comment.CreateCommentResponse;
import com.CapstoneProject.capstone.dto.response.comment.GetCommentResponse;
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
import java.util.*;

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
        sessions.add(session);
        log.info("Client connected: {}", session.getId());
        session.sendMessage(new TextMessage("{\"message\": \"Connected, please send token and questionId\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, String> data = objectMapper.readValue(message.getPayload(), Map.class);
            String token = data.get("token");
            String action = data.get("action");
            String questionId = data.get("questionId");
            String content = data.get("content");

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
            log.info("Client authenticated: {} with username: {}", session.getId(), username);

            if (questionId != null && !questionId.trim().isEmpty()) {
                session.getAttributes().put("questionId", UUID.fromString(questionId));
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            user, null, userDetails.getAuthorities()));

            if ("getComments".equals(action)) {
                if (questionId == null) {
                    session.sendMessage(new TextMessage("{\"error\": \"questionId is required for getComments\"}"));
                    return;
                }
                List<GetCommentResponse> comments = commentService.getAllComments(UUID.fromString(questionId));
                String jsonResponse = objectMapper.writeValueAsString(comments);
                session.sendMessage(new TextMessage(jsonResponse));
                log.info("Sent comments for questionId: {}", questionId);
            } else if ("createComment".equals(action) || action == null) {
                if (questionId == null || content == null || content.trim().isEmpty()) {
                    return;
                }
                CreateCommentRequest request = new CreateCommentRequest();
                request.setContent(content);
                CreateCommentResponse response = commentService.createComment(UUID.fromString(questionId), request);
                broadcastComment(response, UUID.fromString(questionId));
                log.info("Broadcasted comment: {}", response.getContent());
            } else {
                session.sendMessage(new TextMessage("{\"error\": \"Unknown action: " + action + "\"}"));
            }
        } catch (Exception e) {
            log.error("Error handling message from session {}: {}", session.getId(), e.getMessage());
            session.sendMessage(new TextMessage("{\"error\": \"Failed to process message: " + e.getMessage() + "\"}"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("Client disconnected: {} with status: {}", session.getId(), status);
    }

    public void broadcastComment(CreateCommentResponse comment, UUID questionId) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(comment);
        synchronized (sessions) {
            log.info("Broadcasting to {} sessions for questionId: {}", sessions.size(), questionId);
            for (WebSocketSession session : sessions) {
                UUID subscribedQuestionId = (UUID) session.getAttributes().get("questionId");
                if (session.isOpen() && subscribedQuestionId != null && subscribedQuestionId.equals(questionId)) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                        log.info("Sent to session {} for questionId: {}", session.getId(), questionId);
                    } catch (IOException e) {
                        log.error("Failed to send to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            }
        }
    }
}