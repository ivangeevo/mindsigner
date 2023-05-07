package hive.ivangeevo.mindsigner;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


import javax.net.ssl.SSLContext;
import org.glassfish.tyrus.core.TyrusServerEndpointConfig;

public class CraftSocketServer {
    private ServerSocket serverSocket;
    


    public void start() throws IOException {

    }

    public Socket accept() throws IOException {
        return serverSocket.accept();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public boolean isRunning() {
        return true;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Start the server on a separate thread
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8080, 0, InetAddress.getByName("localhost"));
                System.out.println("CraftSocketServer started on port " + serverSocket.getLocalPort());

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());

                    // Handle the incoming clientSocket here
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

