package hive.ivangeevo.mindsigner;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class CraftSocketServerConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // Get the HTTP session
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        // Check if the user is authenticated
        if (httpSession.getAttribute("authenticated") == null || !(Boolean) httpSession.getAttribute("authenticated")) {
            // If the user is not authenticated, throw an exception to prevent the WebSocket connection from being established
            throw new SecurityException("User not authenticated");
        }
    }
}