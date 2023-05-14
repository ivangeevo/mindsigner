package hive.ivangeevo.mindsigner.craftsocket;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;


@ServerEndpoint(value = "/", configurator = CSServerConfigurator.class, subprotocols = {"protocol1", "protocol2"})
public class CSWebsocketServer {
    private static final Logger LOGGER = Logger.getLogger(CSWebsocketServer.class.getName());
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private static CSComponentProvider.FeatureImpl.TyrusEndpointPublisher serverEndpointPublisher;
    private final InetSocketAddress address;



    public CSWebsocketServer(InetSocketAddress address) {
        this.address = address;
    }





    private HttpsServer createHttpsServer(InetSocketAddress address, SSLContext sslContext) throws IOException {
        HttpsServer httpsServer = HttpsServer.create(address, 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        httpsServer.createContext("/");
        httpsServer.setExecutor(getThreadPoolExecutor());
        return httpsServer;
    }

    private String handleHttpRequest(HttpExchange exchange)  {


        String key;
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                Headers headers = exchange.getRequestHeaders();
                if (headers.containsKey("Upgrade") && headers.get("Upgrade").get(0).equalsIgnoreCase("websocket")) {
                    key = headers.getFirst("Sec-WebSocket-Key");
                    String response = WebSocketUtil.generateHandshakeResponse(key);
                    exchange.sendResponseHeaders(101, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    LOGGER.info("WebSocket Handshake Successful");
                } else {
                    LOGGER.warning("Invalid HTTP Upgrade Request");
                    exchange.sendResponseHeaders(400, 0);
                }
            } catch (Exception e) {
                LOGGER.severe("Error occurred while handling HTTP request: " + e.getMessage());
            }
        }
        public static String generateHandshakeResponse (String key) throws Exception {
            String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            String concat = key + guid;
            byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(concat.getBytes("UTF-8"));
            return "HTTP/1.1 101 Switching Protocols\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Accept: " + Base64.getEncoder().encodeToString(sha1) + "\r\n\r\n";
        }
    }
    private ThreadPoolExecutor getThreadPoolExecutor() {
        int corePoolSize = 10;
        int maxPoolSize = 100;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maxPoolSize);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }
    public void stop() throws Exception {
        for (Session session : sessions) {
            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Server shutting down"));
        }
        serverEndpointPublisher.stop();
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

    public void start() throws IOException {

    }


    public CSWebsocketServer accept() throws IOException {
        return null;
    }

    public InputStream getInputStream() throws IOException {
        return null;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public SocketAddress getRemoteSocketAddress() {
        return null;
    }

    public void close() throws IOException {
    }

    public boolean isClosed() {
        return false;
    }


    // SSL Context configuration for the server
    private SSLContext createSSLContext() throws Exception {
        char[] passphrase = "password".toCharArray();
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
