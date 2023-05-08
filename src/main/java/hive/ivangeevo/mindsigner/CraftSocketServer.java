package hive.ivangeevo.mindsigner;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.WebSocketContainer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;
import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
//
import jakarta.websocket.*;
//
import java.util.concurrent.Executor;




public class CraftSocketServer {
    private Server server;
    private static final Logger LOGGER = LoggerFactory.getLogger(CraftSocketServer.class);

    public void start() {
        // Create your own SSLContext if required
        TyrusServerEndpointConfig endpointConfig = TyrusServerEndpointConfig.Builder.create(CraftSocketEndpoint.class, "/")
                .build();
    }

    public void stop() {
        server.stop();
        LOGGER.info("CraftSocketServer stopped");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Server server = new Server("localhost", 8080, "/websocket", null, CraftSocketEndpoint.class);
        try {
            server.start();
            LOGGER.info("CraftWebsocketServer started on port {}", server.getPort());
        } catch (DeploymentException e) {
            LOGGER.error("Failed to start CraftWebsocketServer", e);
        }
    }

    public static void main(String[] args) {
        try {
            new CraftSocketServer().start();
        } catch (Exception e) {
            LOGGER.error("Failed to start CraftWebsocketServer", e);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // Implement this method if needed
    }
}