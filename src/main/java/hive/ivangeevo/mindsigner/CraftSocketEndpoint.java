package hive.ivangeevo.mindsigner;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;


import org.glassfish.tyrus.client.auth.AuthConfig;
import org.glassfish.tyrus.core.ComponentProviderService;
import org.glassfish.tyrus.core.ReflectionHelper;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.core.TyrusEndpointWrapper;
import org.glassfish.tyrus.core.cluster.ClusterContext;
import org.glassfish.tyrus.core.monitoring.EndpointEventListener;

@ServerEndpoint(value = "/CraftSocketEndpoint", decoders = CraftSocketMain.MyDecoder.class, encoders = CraftSocketMain.MyEncoder.class)

public class CraftSocketEndpoint extends Endpoint {


    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // Check if the initial request is an HTTP request
        if (session.getRequestURI().getScheme().equals("http")) {
            // Send an HTTP response with a "101 Switching Protocols" status code and an "Upgrade" header
            session.getAsyncRemote().sendText("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n\r\n");
        }

        session.addMessageHandler(new MessageHandler.Whole<CSEventMessage>() {
            @Override
            public void onMessage(CSEventMessage message) {
                if ("custom-event".equals(message.getEvent())) {
                    onMessageEvent(message, session);
                } else {
                    // Handle other types of messages
                }
            }

            private void onMessageEvent(CSEventMessage message, Session session) {
                // Handle custom event
            }
        });
    }


    public ServerEndpointConfig.Configurator getConfigurator() {
        return null;
    }

    public void setCraftSocketServer(CraftSocketServer craftSocketServer) {
    }

    public static class CraftSocketEndpointConfig extends TyrusEndpointWrapper {
        /**
         * Create {@link TyrusEndpointWrapper} for class that extends {@link Endpoint}.
         *
         * @param endpointClass            endpoint class for which the wrapper is created.
         * @param configuration            endpoint configuration.
         * @param componentProvider        component provider.
         * @param container                container where the wrapper is running.
         * @param contextPath              context path of the application.
         * @param configurator             endpoint configurator.
         * @param sessionListener          session listener.
         * @param clusterContext           cluster context instance. {@code null} indicates standalone mode.
         * @param endpointEventListener    endpoint event listener.
         * @param parallelBroadcastEnabled {@code true} if parallel broadcast should be enabled, {@code true} is default.
         * @throws DeploymentException when the endpoint is not valid.
         */
        public CraftSocketEndpointConfig(Class<? extends Endpoint> endpointClass, EndpointConfig configuration, ComponentProviderService componentProvider, WebSocketContainer container, String contextPath, ServerEndpointConfig.Configurator configurator, SessionListener sessionListener, ClusterContext clusterContext, EndpointEventListener endpointEventListener, Boolean parallelBroadcastEnabled) throws DeploymentException {
            super(endpointClass, configuration, componentProvider, container, contextPath, configurator, sessionListener, clusterContext, endpointEventListener, parallelBroadcastEnabled);
        }
    }

    TyrusServerEndpointConfig endpointConfig = (TyrusServerEndpointConfig) AuthConfig.Builder.create().build();
    private ReflectionHelper endpoint;
    // now you can deploy the endpoint

    @OnMessage
    public void onEventMessage(CSEventMessage message, Session session) {
        String eventName = message.getEvent();
        // Handle the custom event
    }
}





