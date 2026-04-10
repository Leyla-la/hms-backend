package com.hms.notification.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class PrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Try profileId first, then userId
        String profileId = (String) attributes.get("profileId");
        String userId = (String) attributes.get("userId");
        String role = (String) attributes.get("role");
        
        String idPart = (profileId != null && !profileId.trim().isEmpty()) ? profileId : userId;
        String rolePart = (role != null && !role.trim().isEmpty()) ? role.toUpperCase() : "UNKNOWN";
        
        if (idPart != null && !idPart.trim().isEmpty()) {
            final String identifier = rolePart + ":" + idPart;
            return () -> identifier;
        }
        
        return super.determineUser(request, wsHandler, attributes);
    }
}
