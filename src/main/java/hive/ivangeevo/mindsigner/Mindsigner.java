
package hive.ivangeevo.mindsigner;

import jakarta.websocket.*;
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

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.stream.Collectors;


@Mod("mindsigner")
@Mod.EventBusSubscriber(modid = "mindsigner", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Mindsigner {
    private static final Logger LOGGER = LogManager.getLogger(Mindsigner.class);
    private static final UUID dummyUUID = UUID.randomUUID();
    private static CSWebsocketServer socketServer;






    public Mindsigner() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static class CSWebsocketServer extends Thread {



        private boolean running = false;
        private boolean connected = false;
        private String ip;

        public CSWebsocketServer(String ip, int port) {
            this.ip = ip;
        }

        private void close() {
        }


        private Object getPort() {
            return null;
        }

        private Object getHost() {
            return null;
        }

        private OutputStream getOutputStream() {
            return null;
        }

        private InputStream getInputStream() {
            return null;
        }

        private Throwable getRemoteSocketAddress() {
            return null;
        }

        private CSWebsocketServer accept() {
            return null;
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
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                Socket clientSocket = serverSocket.accept();
                                // handle the new client connection here
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }



    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    }

}