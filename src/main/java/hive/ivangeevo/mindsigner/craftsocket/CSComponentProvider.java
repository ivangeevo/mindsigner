package hive.ivangeevo.mindsigner.craftsocket;

import jakarta.websocket.server.ServerEndpointConfig;
import org.glassfish.tyrus.core.*;

import javax.websocket.server.ServerEndpointConfig.Configurator;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CSComponentProvider extends Configurator {

    private static ComponentProviderService providerService = null;

    public CSComponentProvider(ComponentProviderService providerService) {
        CSComponentProvider.providerService = providerService;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return null;
    }

    public static class FeatureImpl implements javax.ws.rs.core.Feature {

        @Override
        public boolean configure(javax.ws.rs.core.FeatureContext context) {
            context.register(new TyrusEndpointPublisher());
            return true;
        }

        public static class TyrusEndpointPublisher {
            public TyrusEndpointPublisher() {
                List<Class<?>> endpointClasses = Collections.singletonList(TyrusWebSocketEngine.class);
                ServiceFinder<ComponentProvider> finder = ServiceFinder.find(ComponentProvider.class);
                List<ComponentProvider> providers = new ArrayList<>();
                for (ComponentProvider componentProvider : finder) {
                    providers.add(componentProvider);
                }
                providers.add(new DefaultComponentProvider());
                providerService = ComponentProviderService.create();
            }

            public ComponentProviderService ProviderService() {
                return providerService;
            }

            public void setEndpoint(Class<?> endpointClass) {
            }

            public void onOpen(ServerEndpointConfig endpointConfig, TyrusWebSocketEngine engine) {
            }

            public void start(InetSocketAddress localhost, List<ServerEndpointConfig> serverEndpointConfigs) {
            }

            public void stop() {
            }
        }

    }
}
