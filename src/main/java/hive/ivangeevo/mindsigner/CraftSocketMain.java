
package hive.ivangeevo.mindsigner;

import com.google.gson.Gson;
import jakarta.websocket.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;


@Mod("mindsigner")
@Mod.EventBusSubscriber(modid = "mindsigner", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftSocketMain {
    private static final Logger LOGGER = LogManager.getLogger(CraftSocketMain.class);
    private static final UUID dummyUUID = UUID.randomUUID();
    private static CraftSocketServer socketServer;

    public CraftSocketMain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public class MyEncoder<MyMessage> implements Encoder.Text<MyMessage> {
        @Override
        public void init(EndpointConfig config) {
            // Initialization logic
        }

        @Override
        public void destroy() {
            // Cleanup logic
        }

        @Override
        public String encode(MyMessage object) throws EncodeException {
            return null;
        }
    }

    public class MyDecoder<MyMessage> implements Decoder.Text<MyMessage> {
        @Override
        public void init(EndpointConfig config) {
            // Initialization logic
        }

        @Override
        public void destroy() {
            // Cleanup logic
        }


        @Override
        public MyMessage decode(String s) throws DecodeException {
            return null;
        }

        @Override
        public boolean willDecode(String s) {
            return false;
        }

    }

    public static class CraftSocketServer {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final Gson gson = new Gson();

        private ServerSocket serverSocket;
        private Socket socket;
        private ServerPlayer player;
        private boolean running = false;
        private boolean connected = false;
        private String ip;
        private int port;
        private String username;
        private String password;
        private String uuid;
        private String name;

        public CraftSocketServer(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public void start() {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(ip, port));
                LOGGER.info("WebSocket server started and listening on {}:{}", ip, port);

                while (true) {
                    socket = serverSocket.accept();
                    LOGGER.info("Client connected: {}", socket.getInetAddress().getHostAddress());

                    // Create input and output streams for the socket
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                    // Read a UTF-8 encoded string from the client
                    String message = in.readUTF();
                    LOGGER.info("Received message from client: {}", message);

                    // Write a UTF-8 encoded string to the client
                    out.writeUTF("Hello from server!");

                    // Close the input and output streams and the socket
                    in.close();
                    out.close();
                    socket.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to start WebSocket server", e);
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void stop() {
            try {
                if (connected) {
                    socket.close();
                }
                serverSocket.close();
                running = false;
                connected = false;
            } catch (IOException e) {
                LOGGER.error("Failed to stop WebSocket server", e);
            }
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("mindsigner", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) throws IOException {
        if (event.getPlayer() instanceof ServerPlayer) {
            if (socketServer == null) {
                // Create a new CraftSocketServer object with the desired IP address and port number
                socketServer = new CraftSocketServer("localhost", 8443);
            }
            if (!socketServer.isRunning()) {
                // Start the WebSocket server using the CraftSocketServer object
                socketServer.start();
                TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8080");
                event.getPlayer().sendMessage(message, dummyUUID);
            } else {
                TextComponent message = new TextComponent("WebSocket server is already running");
                event.getPlayer().sendMessage(message, dummyUUID);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer) {
            if (socketServer != null && socketServer.isRunning()) {
                TextComponent message = new TextComponent("WebSocket server stopped");
                event.getPlayer().sendMessage(message, dummyUUID);
            }
        }
        if (socketServer != null) {
            try {
                socketServer.stop();
                LOGGER.info("WebSocket server stopped");
            } catch (Exception e) {
                LOGGER.error("Failed to stop websocket server", e);
            }
        }
    }

    public void run() throws Exception {
        while (!Thread.currentThread().isInterrupted()) {
            // Handle incoming client connections and messages
            // This could be done using the CraftSocketServer class or a separate class

            // Add some statements here
            // For example:
            System.out.println("Waiting for clients...");
            Thread.sleep(1000);
        }
    }
}