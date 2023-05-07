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

import java.io.FileInputStream;
import java.io.IOException;


import java.security.KeyStore;
import java.security.SecureRandom;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


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



    private static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("keystore.p12"), "password".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "password".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

        return sslContext;
    }




    private void setup(final FMLCommonSetupEvent event) {
        try {
            int port = 8080;
            socketServer = new CraftSocketServer();
            socketServer.start();
            LOGGER.info("WebSocket server started and listening on localhost:8080");
        } catch (Exception e) {
            LOGGER.error("Failed to start websocket server", e);
        }
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
        int port = 8080;
        try {
            ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
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
        } catch (IOException e) {
            LOGGER.error("Failed to start CraftSocketServer", e);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer) {
            if (socketServer != null && socketServer.isRunning()) {
                TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8080");
                event.getPlayer().sendMessage(message, dummyUUID);
            }
        }

        if (socketServer == null)

            try {
                int port = 8080;
                socketServer = new CraftSocketServer();
                socketServer.start();

                LOGGER.info("WebSocket server started and listening on localhost:8080");

                // Send a chat message to the player
                TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8080");
                event.getPlayer().sendMessage(message, dummyUUID);
            } catch (Exception e) {
                LOGGER.error("Failed to start websocket server", e);
            }
    }


        @SubscribeEvent
        public void onPlayerLoggedOut (PlayerEvent.PlayerLoggedOutEvent event){
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