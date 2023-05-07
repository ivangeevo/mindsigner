package hive.ivangeevo.mindsigner;

import jakarta.websocket.DeploymentException;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.glassfish.tyrus.server.Server;

public class CraftSocketServer {

    public void start() throws DeploymentException {
        Server server = new Server("localhost", 8080, "/websocket", null, CraftSocketEndpoint.class);
        server.start();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Start the server on a separate thread
        new Thread(() -> {
            try {
                start();
            } catch (DeploymentException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean isRunning() {
        return false;
    }

    public void stop() {
    }
}
