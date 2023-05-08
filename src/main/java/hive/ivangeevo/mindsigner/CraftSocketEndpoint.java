package hive.ivangeevo.mindsigner;

import jakarta.websocket.*;

import java.util.List;
import java.util.Map;

import jakarta.websocket.server.ServerEndpointConfig;
import org.glassfish.tyrus.client.auth.AuthConfig;
import org.glassfish.tyrus.core.ComponentProviderService;
import org.glassfish.tyrus.core.ReflectionHelper;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.core.TyrusEndpointWrapper;
import org.glassfish.tyrus.core.cluster.ClusterContext;
import org.glassfish.tyrus.core.monitoring.EndpointEventListener;

public class CraftSocketEndpoint extends Endpoint {

    public ServerEndpointConfig.Configurator getConfigurator() {
        return null;
    }

    public class CraftSocketEndpointConfig extends TyrusEndpointWrapper {
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

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                // Handle message
            }
        });
    }

    TyrusServerEndpointConfig endpointConfig = (TyrusServerEndpointConfig) AuthConfig.Builder.create().build();
    private ReflectionHelper endpoint;
    // now you can deploy the endpoint
}




