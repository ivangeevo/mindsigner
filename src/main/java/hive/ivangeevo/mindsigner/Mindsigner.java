
package hive.ivangeevo.mindsigner;

import hive.ivangeevo.mindsigner.craftsocket.CSWebsocketServer;
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

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.ServerSocket;
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

    public void start(SSLContext sslContext) {
        ServerSocket serverSocket = null;
        try {
            int port = 8443;
            String ip = "localhost";
            serverSocket = sslContext.getServerSocketFactory().createServerSocket(port);

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
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) throws Exception {
        if (socketServer == null) {
            SSLContext sslContext = socketServer.createSSLContext();
            socketServer.start(sslContext);

            TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8443");
            event.getPlayer().sendMessage(message, Util.NIL_UUID);
        } else {
            TextComponent message = new TextComponent("WebSocket server is already running");
            event.getPlayer().sendMessage(message, Util.NIL_UUID);
        }
    }






    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    }

}