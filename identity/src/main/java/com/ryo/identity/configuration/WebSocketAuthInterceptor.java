package com.ryo.identity.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            Authentication auth = (Authentication) accessor.getUser();
            if (auth == null) {
                throw new RuntimeException("Unauthorized WebSocket connection");
            }

            String destination = accessor.getDestination();

            boolean isAdmin = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ADMIN"));

            // USER không được phép vào admin channel
            if (destination.startsWith("/topic/admin") && !isAdmin) {
                throw new RuntimeException("Access Denied: User cannot subscribe to admin topic");
            }

            // Nếu là USER → cấm subscribe của user khác
            if (destination.startsWith("/topic/user.")) {
                String username = auth.getName();
                if (!destination.equals("/topic/user." + username) && !isAdmin) {
                    throw new RuntimeException("Access Denied: Cannot read other user's notifications");
                }
            }
        }

        return message;
    }
}
