package hive.ivangeevo.mindsigner;

import hive.ivangeevo.mindsigner.xmindsigner.CSServerConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.core.TyrusEndpointWrapper;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ServerEndpoint(value = "/", configurator = CSServerConfigurator.class, subprotocols = {"protocol1", "protocol2"})
public class CraftSocketServer {
    private static final Logger LOGGER = Logger.getLogger(CraftSocketServer.class.getName());
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private static TyrusEndpointWrapper serverEndpoint;

    public CraftSocketServer() {}

    public void start() throws Exception {
        SSLContext sslContext = createSSLContext();
        serverEndpoint = new TyrusEndpointWrapper(null, null, sslContext, null, null, null, null);
        serverEndpoint.setEndpoint(CraftSocketServer.class);
        serverEndpoint.start();
        LOGGER.info("CraftSocketServer started");
    }

    public void stop() throws Exception {
        for (Session session : sessions) {
            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Server shutting down"));
        }
        serverEndpoint.stop();
        LOGGER.info("CraftSocketServer stopped");
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        sessions.add(session);
        LOGGER.info("New session opened: " + session.getId());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        sessions.remove(session);
        LOGGER.info("Session closed: " + session.getId() + ", Reason: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.severe("Error occurred in session: " + session.getId() + ", Error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Received message from " + session.getId() + ": " + message);
    }

    private SSLContext createSSLContext() throws Exception {
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        InputStream is = getClass().getClassLoader().getResourceAsStream("keystore.p12");
        ks.load(is, passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }
}
