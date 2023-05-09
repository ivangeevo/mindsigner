package hive.ivangeevo.mindsigner;

import com.google.gson.Gson;
import jakarta.websocket.Decoder;
import jakarta.websocket.Encoder;
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ibm.icu.text.PluralRules.Operand.e;


@Mod("mindsigner")
@Mod.EventBusSubscriber(modid = "mindsigner", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Mindsigner {
    private static final Logger LOGGER = LogManager.getLogger(Mindsigner.class);
    private static final UUID dummyUUID = UUID.randomUUID();
    private static CraftSocketServer socketServer;

    public Mindsigner() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static class GsonEncoder implements Encoder.Text<Object> {
        private final Gson gson = new Gson();

        public GsonEncoder(Gson gson) {
        }

        @Override
        public String encode(Object object) {
            return gson.toJson(object);
        }
    }

    public static class GsonDecoder implements Decoder.Text<Object> {
        private final Gson gson = new Gson();

        public GsonDecoder(Gson gson) {
        }

        @Override
        public Object decode(String s) {
            return gson.fromJson(s, Object.class);
        }

        @Override
        public boolean willDecode(String s) {
            return true;
        }
    }
    public static class CraftSocketServer {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final Gson gson = new Gson();
        private final GsonEncoder gsonEncoder = new GsonEncoder(gson);
        private final GsonDecoder gsonDecoder = new GsonDecoder(gson);
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


        }

        public boolean isRunning() {
            return false;
        }

        public void stop() {
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
        int port = 8080;
        ServerSocket serverSocket;
        serverSocket = new ServerSocket();
        try {
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        LOGGER.info("CraftSocketServer started on port {}", serverSocket.getLocalPort());
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("Client connected from {}", clientSocket.getInetAddress().getHostAddress());
                // Handle the incoming clientSocket here
            } catch (IOException e) {
                LOGGER.error("Error accepting client connection", e);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        if (event.getPlayer() instanceof ServerPlayer) {
            if (socketServer != null && socketServer.isRunning()) {
                TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8080");
                event.getPlayer().sendMessage(message, dummyUUID);
            } else  {
                LOGGER.error("Failed to start CraftSocketServer", e);
                return;
            }
        }
    }


        public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event){
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


        public void run () throws Exception {
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