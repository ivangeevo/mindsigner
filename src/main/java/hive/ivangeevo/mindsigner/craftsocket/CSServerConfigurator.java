package hive.ivangeevo.mindsigner.craftsocket;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class CSServerConfigurator extends ServerEndpointConfig.Configurator {
    private static final String CRAFT_SOCKET_SERVER_KEY = "CraftSocketServer";
    private EndpointConfig endpointConfig;

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // Store the CraftSocketServer instance in the endpoint configuration
        HttpSession session = (HttpSession) request.getHttpSession();
        ServletContext servletContext = session.getServletContext();
        CSWebsocketServer craftSocketServer = (CSWebsocketServer) servletContext.getAttribute(CRAFT_SOCKET_SERVER_KEY);
        sec.getUserProperties().put(CRAFT_SOCKET_SERVER_KEY, craftSocketServer);
        this.endpointConfig = sec;
    }

    private EndpointConfig getEndpointConfig() {
        return this.endpointConfig;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        // Check if the endpoint class is the CraftSocketEndpoint
        if (endpointClass.equals(CSEndpoint.class)) {
            // Get the CraftSocketServer instance from the endpoint configuration
            CSWebsocketServer craftSocketServer = (CSWebsocketServer) getEndpointConfig().getUserProperties().get(CRAFT_SOCKET_SERVER_KEY);

            // Create a new CraftSocketEndpoint instance with the CraftSocketServer instance injected
            CSEndpoint craftSocketEndpoint = new CSEndpoint();
            craftSocketEndpoint.setCraftSocketServer(craftSocketServer);
            return (T) craftSocketEndpoint;
        } else {
            throw new InstantiationException("Invalid endpoint class: " + endpointClass);
        }
    }
}
