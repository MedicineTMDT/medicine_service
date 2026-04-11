package com.ryo.identity.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final CustomJwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthorityPrefix("");
                
                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                
                Authentication auth = jwtAuthenticationConverter.convert(jwt);
                accessor.setUser(auth);
            }
        }

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            Authentication auth = (Authentication) accessor.getUser();
            if (auth == null) {
                throw new RuntimeException("Unauthorized WebSocket connection");
            }

            String destination = accessor.getDestination();
            if (destination != null) {
                boolean isAdmin = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role -> role.equals("ADMIN"));

                if (destination.startsWith("/topic/admin") && !isAdmin) {
                    throw new RuntimeException("Access Denied: User cannot subscribe to admin topic");
                }

                if (destination.startsWith("/topic/user.")) {
                    String username = auth.getName();
                    if (!destination.equals("/topic/user." + username) && !isAdmin) {
                        throw new RuntimeException("Access Denied: Cannot read other user's notifications");
                    }
                }
            }
        }

        return message;
    }
}
