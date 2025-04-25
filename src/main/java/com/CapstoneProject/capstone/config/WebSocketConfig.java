package com.CapstoneProject.capstone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CommentWebSocketHandler commentWebSocketHandler;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public WebSocketConfig(CommentWebSocketHandler commentWebSocketHandler, NotificationWebSocketHandler notificationWebSocketHandler) {
        this.commentWebSocketHandler = commentWebSocketHandler;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(commentWebSocketHandler, "/comment")
                .setAllowedOrigins("*");
        registry.addHandler(notificationWebSocketHandler, "/notification")
                .setAllowedOrigins("*");
    }
}
