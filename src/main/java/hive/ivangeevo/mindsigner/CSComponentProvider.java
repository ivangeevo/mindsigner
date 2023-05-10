package hive.ivangeevo.mindsigner;

import org.glassfish.tyrus.core.ComponentProvider;
import org.glassfish.tyrus.core.ComponentProviderService;
import org.glassfish.tyrus.core.DefaultComponentProvider;

import javax.websocket.server.ServerEndpointConfig.Configurator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSComponentProvider extends Configurator {

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return null;
    }

    public CSComponentProvider() {
        List<ComponentProvider> providers = new ArrayList<>();
        providers.add(new DefaultComponentProvider());

        ComponentProviderService providerService = ComponentProviderService.builder()
                .customProviders(providers)
                .build();
    }

    public static class FeatureImpl implements javax.ws.rs.core.Feature {

        @Override
        public boolean configure(javax.ws.rs.core.FeatureContext context) {
            context.register(new TyrusEndpointPublisher());
            return true;
        }

        private static class TyrusEndpointPublisher {
        }
    }
}
