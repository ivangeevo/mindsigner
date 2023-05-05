package hive.ivangeevo.mindsigner;

import com.mojang.logging.LogUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


import java.security.Security;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.stream.Collectors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;
import java.util.UUID;

import net.minecraftforge.event.entity.player.PlayerEvent;






@Mod("mindsigner")
public class Mindsigner {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Mindsigner() throws Exception {




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
        // Add your code here to start listening on the desired port

        int port = 8080;
        CraftSocketServer server = new CraftSocketServer(port, true);
        server.run();
        LOGGER.info("Websocket Started and listening on localhost:8080");


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
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) throws Exception {
        // You can also access the player entity using event.player if needed

        Player player = event.getPlayer();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            UUID uuid = serverPlayer.getUUID();
            serverPlayer.sendMessage(new TextComponent("The server is running on localhost:8080"), uuid);

        }
        // Check if server is already running on localhost:8080
        boolean serverRunning = false;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals("CraftSocketServer") && t.isAlive()) {
                serverRunning = true;
                break;
            }
        }
        if (!serverRunning) {
            int port = 8080;
            CraftSocketServer server = new CraftSocketServer(port, true);
            server.run();
            LOGGER.info("WebSocket server started and listening on localhost:8080");

            // Get the player entity from the event
            UUID uuid = player.getUUID();

            // Send a chat message to the player
            player.sendMessage(new TextComponent("The server is running on localhost:8080"), uuid);
        }
        else {
            LOGGER.info("WebSocket server already running on localhost:8080");
        }
    }

}

