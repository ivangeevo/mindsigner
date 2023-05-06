package hive.ivangeevo.mindsigner;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.util.UUID;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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

@Mod("mindsigner")
@Mod.EventBusSubscriber(modid = "mindsigner", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Mindsigner {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final UUID dummyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private CraftSocketServer server;

    public Mindsigner() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO FROM SERVER STARTING");
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        LOGGER.info("HELLO FROM REGISTER BLOCKS");
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        LOGGER.info("HELLO FROM PLAYER JOIN");
    }

    private void setup(final FMLCommonSetupEvent event) {
        int port = 8080;
        try {
            server = new CraftSocketServer(port, message -> {
                System.out.println("Received message from client: " + message);
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

            // Event loop groups for the Netty server
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            // Load SSL certificate for secure WebSocket connections
            SslContext sslContext = null;
            try {
                sslContext = SslContextBuilder.forServer(new File("my-server.pem"), new File("my-server.key"))
                        .build();
            } catch (SSLException e) {
                LOGGER.error("Failed to load SSL certificate", e);
            }

        SslContext finalSslContext = sslContext;
        ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (finalSslContext != null) {
                                pipeline.addLast(finalSslContext.newHandler(ch.alloc()));
                            }
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/websocket", null, true));
                            pipeline.addLast(new CraftSocketServerHandler("localhost", server));
                        }
                    });

            // Bind and start the server
            ChannelFuture future = bootstrap.bind(port).sync();
            LOGGER.info("WebSocket server started and listening on localhost:" + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("Failed to start WebSocket server", e);
        }

        // Some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}