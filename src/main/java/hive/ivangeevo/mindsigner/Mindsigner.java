
package hive.ivangeevo.mindsigner;

import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
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
import java.net.ServerSocket;
import java.util.UUID;
import java.util.stream.Collectors;


@Mod("mindsigner")
@Mod.EventBusSubscriber(modid = "mindsigner", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Mindsigner {
    private static final Logger LOGGER = LogManager.getLogger(Mindsigner.class);
    private static CSWebsocketServer socketServer;


    public Mindsigner() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(this);
    }


    public static class CSWebsocketServer {



        private boolean running = false;
        private boolean connected = false;

        public CSWebsocketServer(String ip, int port) {
        }

        private void close() {
        }


        public boolean isRunning() {
            return running;
        }

        public void stop() {
            if (connected) {
                socketServer.close();
            }
            socketServer.close();
            running = false;
            connected = false;
        }

        public void start() {
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

    public void start() {
        ServerSocket serverSocket = null;
        try {
            int port = 8443;
            String ip = "localhost";
            serverSocket = new ServerSocket();
            
            // Rest of the code here
        } catch (IOException e) {
            LOGGER.error("Failed to start WebSocket server", e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing server socket", e);
                }
            }
        }
    }




    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) throws IOException {
        if (event.getPlayer() != null) {
            if (socketServer == null) {
                // Create a new CSWebsocketServer object with the desired IP address and port number
                socketServer = new CSWebsocketServer("localhost", 8443);
            }
            if (!socketServer.isRunning()) {
                // Start the WebSocket server using the CSWebsocketServer object on a separate thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketServer.start();
                    }
                }).start();
                TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8443");
                event.getPlayer().sendMessage(message, Util.NIL_UUID);
            } else {
                TextComponent message = new TextComponent("WebSocket server is already running");
                event.getPlayer().sendMessage(message, Util.NIL_UUID);
            }
        }
    }



    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    }

}