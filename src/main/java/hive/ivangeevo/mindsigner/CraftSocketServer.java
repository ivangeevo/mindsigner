package hive.ivangeevo.mindsigner;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;

import java.security.KeyStore;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.jetbrains.annotations.NotNull;

import java.util.logging.*;



public class CraftSocketServer {

    protected static final String KEYSTORE_PATH = "C:/MINDCRAFT PROJECTS/mindsigner/SocketCluster/keystore.p12";
    protected static final String KEYSTORE_PASSWORD = "password";
    protected final int port;

    protected static final String SERVER_HOSTNAME = "localhost";

    protected static final Logger logger = Logger.getLogger(CraftSocketServer.class.getName());

    protected final EventLoopGroup bossGroup;
    protected final EventLoopGroup workerGroup;



    public static void main(String[] args) throws Exception {

        CraftSocketServer server = new CraftSocketServer(8080);
        server.run();
    }

    public CraftSocketServer(int port) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        this.port = port;
    }

    public void run() throws Exception {

        // Load keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            ks.load(fis, KEYSTORE_PASSWORD.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, KEYSTORE_PASSWORD.toCharArray());

        // Create SSL context
        SslContext sslCtx = SslContextBuilder.forServer(kmf).build();

        try {
            // Configure the server bootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(this.bossGroup, this.workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {


                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) {

                            ChannelPipeline pipeline = ch.pipeline();

                            logger.info("Starting WebSocket handshake...");

                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer("Hello World".getBytes()));
                            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

                            HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/websocket");
                            req.headers().add(HttpHeaderNames.HOST, "localhost:" + port);

                            // Add SSL handler to pipeline
                            SSLEngine sslEngine = sslCtx.newEngine(ch.alloc());
                            pipeline.addLast(new SslHandler(sslEngine));
                            // Add HTTP codecs to pipeline
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));

                            // Add WebSocket server protocol handler to pipeline
                            String WEBSOCKET_PATH = "";
                            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                                    "ws://" + ch.localAddress().getHostName() + ":" + ch.localAddress().getPort() + WEBSOCKET_PATH, null, true, 65536);
                            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
                            if (handshaker == null) {
                                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ch);
                            } else {
                                ChannelFuture handshakeFuture = handshaker.handshake(ch, req);
                                handshakeFuture.addListener((ChannelFutureListener) future -> {
                                    if (future.isSuccess()) {
                                        logger.info("WebSocket handshake completed successfully!");
                                    } else {
                                        logger.log(Level.WARNING, "WebSocket handshake failed", future.cause());
                                    }
                                });
                            }

                            pipeline.addLast(new CraftSocketServerHandler());
                        }
                    });


            ChannelFuture f = serverBootstrap.bind(SERVER_HOSTNAME, port).sync();
            logger.info("WebSocket server started on host " + SERVER_HOSTNAME + " and port " + port);

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


    public boolean isRunning() {
        return !bossGroup.isShuttingDown() && !workerGroup.isShuttingDown();
    }

}