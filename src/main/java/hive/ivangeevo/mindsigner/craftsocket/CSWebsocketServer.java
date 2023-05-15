package hive.ivangeevo.mindsigner.craftsocket;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.websocket.*;

import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.glassfish.tyrus.server.Server;


@ServerEndpoint(value = "/CraftSocketEndpoint", configurator = CSServerConfigurator.class, subprotocols = {"protocol1", "protocol2"})
public class CSWebsocketServer extends Server {

    private static final Logger LOGGER = Logger.getLogger(CSWebsocketServer.class.getName());
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    private Session session;

    public CSWebsocketServer() {
    }

    private HttpsServer createHttpsServer(InetSocketAddress address, SSLContext sslContext) throws IOException {
        HttpsServer httpsServer = HttpsServer.create(address, 10); // max backlog of 10
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        httpsServer.createContext("/");
        httpsServer.setExecutor(getThreadPoolExecutor());
        return httpsServer;
    }

    private void handleHttpRequest(HttpExchange exchange) {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                Headers headers = exchange.getRequestHeaders();
                if (headers.containsKey("Upgrade") && headers.get("Upgrade").get(0).equalsIgnoreCase("websocket")) {
                    String key = headers.getFirst("Sec-WebSocket-Key");
                    String response = generateHandshakeResponse(key);
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
    }

    private String generateHandshakeResponse(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String concat = key + guid;
        byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(concat.getBytes("UTF-8"));
        return "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + Base64.getEncoder().encodeToString(sha1) + "\r\n\r\n";
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


    // SSL Context configuration for the server
    public SSLContext createSSLContext() throws Exception {
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

        int port = 8080;
        String hostname = "localhost";
        InetSocketAddress address = new InetSocketAddress(hostname, port);

        HttpsServer httpsServer = createHttpsServer(address, sslContext);


        httpsServer.start();

        return sslContext;
    }

    public void handleMessage(String message) {
        try {
            CSEndpoint.sendToAllConnectedSessions(message);
        } catch (IOException e) {
            // Handle the exception
        }
    }

    public boolean isRunning() {
        return false;
    }


    public void start(SSLContext sslContext) {
        try {
            // Create new instance of server
            Map<String, Object> properties = new HashMap<>();
            Server server = new Server();

            // Start the server
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}