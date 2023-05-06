package hive.ivangeevo.mindsigner;

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

import java.util.stream.Collectors;
import java.util.UUID;

@Mod("mindsigner")
@Mod.EventBusSubscriber(modid = "mindsigner", bus = Mod.EventBusSubscriber.Bus.FORGE)

public class Mindsigner {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(Mindsigner.class);
    static final UUID dummyUUID = UUID.randomUUID();
    private static CraftSocketServer socketServer;

    // Define dummyUUID constant

    public Mindsigner() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        int port = 8080;
        try {
            // Create a new CraftSocketServer object and start it in a separate thread
            CraftSocketServer server = new CraftSocketServer(port, true);
            server.start();

            LOGGER.info("Websocket Started and listening on localhost:8080");
        } catch (Exception e) {
            LOGGER.error("Failed to start websocket server", e);
        }

        // Some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }


    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("mindsigner", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer) {
            if (socketServer != null && socketServer.isRunning()) {
                TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8080");
                event.getPlayer().sendMessage(message, dummyUUID);
            }
        }


        if (socketServer == null) {
            try {
                int port = 8080;
                socketServer = new CraftSocketServer(port, true);
                socketServer.start();

                LOGGER.info("WebSocket server started and listening on localhost:8080");

                // Send a chat message to the player
                TextComponent message = new TextComponent("WebSocket server started and listening on localhost:8080");
                event.getPlayer().sendMessage(message, dummyUUID);
            } catch (Exception e) {
                LOGGER.error("Failed to start websocket server", e);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (socketServer != null) {
            try {
                socketServer.stop();
                LOGGER.info("WebSocket server stopped");
            } catch (Exception e) {
                LOGGER.error("Failed to stop websocket server", e);
            }
        }
    }


}


