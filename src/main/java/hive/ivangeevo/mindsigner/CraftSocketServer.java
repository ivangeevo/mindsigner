package hive.ivangeevo.mindsigner;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig.Configurator;
import javax.websocket.server.ServerEndpointConfig.Builder;

import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.server.Server;
import jakarta.websocket.server.ServerEndpointConfig;
import org.glassfish.tyrus.container.grizzly.server.GrizzlyServerContainer;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;



public class CraftSocketServer {

    private static final Logger LOGGER = Logger.getLogger(CraftSocketServer.class.getName());

    private Server server;
    private static Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private final String[] subprotocols;

    public CraftSocketServer(String[] subprotocols) {
        this.subprotocols = subprotocols;
    }



    public void start() throws Exception {
        SSLContext sslContext = createSSLContext();

        TyrusServerEndpointConfig endpointConfig = TyrusServerEndpointConfig.Builder.create(CraftSocketEndpoint.class, "/")
                .subprotocols(List.of(subprotocols))
                .maxSessions(100)
                .configurator(new ServerEndpointConfig.Configurator())
                .build();

        Map<String, Object> properties = new HashMap<>();
        properties.put("org.glassfish.tyrus.incomingBufferSize", 1024 * 1024);

        Set<TyrusServerEndpointConfig> endpointConfigs = Collections.singleton(endpointConfig);

        Socket socket = new Socket("localhost", 8443);
        InetSocketAddress address = new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort());

        Server server = new GrizzlyServerContainer(
                new URI("wss://localhost:8443"),
                new ResourceConfig().registerClasses(CraftSocketEndpoint.class),
                true,
                properties,
                sslContext
        );
        server.start();
        LOGGER.info("CraftSocketServer started on port 8443");
    }


    public void stop() throws Exception {
        for (Session session : sessions) {
            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Server shutting down"));
        }
        server.stop();
    }

    public void broadcast(String message) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                RemoteEndpoint.Basic remote = session.getBasicRemote();
                try {
                    remote.sendText(message);
                } catch (Exception e) {
                    LOGGER.warning("Failed to send message to session " + session.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    private SSLContext createSSLContext() throws Exception {
        // Create a key store with the server certificate
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream keyStoreStream = getClass().getResourceAsStream("/keystore.p12");
        keyStore.load(keyStoreStream, "password".toCharArray());

        // Create a trust store with the client certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream trustStoreStream = getClass().getResourceAsStream("/truststore.jks");
        trustStore.load(trustStoreStream, "password".toCharArray());

        // Create a key manager factory with the key store
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "password".toCharArray());

        // Create a trust manager factory with the trust store
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Create an SSL context with the key manager and trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    @ServerEndpoint(value = "/", configurator = CraftSocketServerConfigurator.class)
    public static class CraftSocketEndpoint {

        private Session session;

        @OnOpen
        public void onOpen(Session session) {
            this.session = session;
            sessions.add(session);
            LOGGER.info("Session " + session.getId() + " opened");
        }

        @OnMessage
        public void onMessage(String message) {
            LOGGER.info("Received message from session " + session.getId() + ": " + message);
        }

        @OnClose
        public void onClose(CloseReason reason) {
            sessions.remove(session);
            LOGGER.info("Session " + session.getId() + " closed: " + reason.getReasonPhrase());
        }

        @OnError
        public void onError(Throwable throwable) {
            LOGGER.warning("Session " + session.getId() + " error: " + throwable.getMessage());
        }
    }

    public static class CraftSocketServerConfigurator extends Configurator {

        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            return endpointClass.cast(new CraftSocketEndpoint());
        }
    }
}
