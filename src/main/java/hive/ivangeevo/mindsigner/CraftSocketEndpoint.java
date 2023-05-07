package hive.ivangeevo.mindsigner;

import jakarta.websocket.*;

import java.util.List;
import java.util.Map;

import jakarta.websocket.server.ServerEndpointConfig;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;

public class CraftSocketEndpoint {

    private final ChatHandler chatHandler;

    public CraftSocketEndpoint() {
        this.chatHandler = new ChatHandler();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        chatHandler.handleMessage();
    }


    public static class CraftSocketEndpointConfig implements TyrusServerEndpointConfig {
        public static void main(String[] args) {
            String serverUrl = "ws://localhost:8080/my-endpoint";
            Endpoint endpoint = new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {

                }
            };
            TyrusServerEndpointConfig endpointConfig = Builder.create(endpoint.getClass(), serverUrl).build();
            ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(endpoint.getClass(), serverUrl).configurator((Configurator) endpointConfig).build();
            // now you can deploy the endpoint
        }



        @Override
        public int getMaxSessions() {
            return 0;
        }

        @Override
        public Class<?> getEndpointClass() {
            return null;
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public List<String> getSubprotocols() {
            return null;
        }

        @Override
        public List<Extension> getExtensions() {
            return null;
        }

        @Override
        public Configurator getConfigurator() {
            return null;
        }

        @Override
        public List<Class<? extends Encoder>> getEncoders() {
            return null;
        }

        @Override
        public List<Class<? extends Decoder>> getDecoders() {
            return null;
        }

        @Override
        public Map<String, Object> getUserProperties() {
            return null;
        }
    }
}
