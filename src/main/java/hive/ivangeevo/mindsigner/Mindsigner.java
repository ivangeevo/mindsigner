package hive.ivangeevo.mindsigner;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
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
import org.glassfish.tyrus.client.SslContextConfigurator;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.glassfish.tyrus.server.Server;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;



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

    private static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
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
            try {
                new Server();
            } catch (Exception e) {
                LOGGER.error("Failed to start CraftWebsocketServer", e);
            }
        }

    private void startSocketServer() throws Exception {

    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) throws Exception {
        SSLContext sslContext = createSSLContext();
        SslContextConfigurator sslContextConfigurator = new SslContextConfigurator();
        sslContextConfigurator.setKeyStoreFile("keystore.p12");
        sslContextConfigurator.setKeyStorePassword("password");
        sslContextConfigurator.setTrustStoreFile("truststore.jks");
        sslContextConfigurator.setTrustStorePassword("password");

        SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(sslContextConfigurator, true, false, false);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        CraftSocketEndpoint socketEndpoint = new CraftSocketEndpoint();
        String uri = "wss://localhost:8080/websocket";
        try { container.connectToServer
                (socketEndpoint, ClientEndpointConfig.Builder.create().build(), new URI(uri));
        } catch (Exception e) { LOGGER.error("Failed to connect to server", e);}
            Server server = new Server("localhost", 8080, "/websocket", (Map<String, Object>) sslEngineConfigurator, CraftSocketEndpoint.class);
        server.start();
        socketServer = new CraftSocketServer();
        LOGGER.info("CraftWebsocketServer started on port {}", server.getPort());
        }



        @SubscribeEvent
        public void onPlayerLoggedOut (PlayerEvent.PlayerLoggedOutEvent event){

            }

        public void run() throws Exception {
            while (!Thread.currentThread().isInterrupted()) {
                // Handle incoming client connections and messages
                // This could be done using the CraftSocketServer class or a separate class

                // Add some statements here
                // For example:
                System.out.println("Waiting for clients...");
            }
        }


    }