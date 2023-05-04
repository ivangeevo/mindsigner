package hive.ivangeevo.mindsigner;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;

public class CraftSocketServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CraftSocketServer.class);

    private final int port;
    private final boolean ssl;

    public CraftSocketServer(int port, boolean ssl) {
        this.port = port;
        this.ssl = ssl;
    }

    public int getPort() {
        return port;
    }

    public void run() throws Exception {
        // Register BouncyCastle as the security provider.
        Security.addProvider(new BouncyCastleProvider());


        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // Add SSL handler first to encrypt and decrypt everything.
                            if (ssl) {
                                SSLContext sslCtx = initSSLContext();
                                ch.pipeline().addLast(new io.netty.handler.ssl.SslHandler(sslCtx.createSSLEngine()));
                            }
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new CraftSocketServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Start the server.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private <with> SSLContext initSSLContext() throws Exception {
        // Load keystore file
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("./src/main/java/resources/keystore.jks"), "keystorePassword".toCharArray());

        // Load truststore file
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream("./src/main/java/resources/truststore.jks"), "truststorePassword".toCharArray());

        // Initialize KeyManagerFactory
        with keystore;
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "keystorePassword".toCharArray());

        // Initialize TrustManagerFactory with truststore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        // Create SSLContext with the KeyManagerFactory and TrustManagerFactory
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }

    public static void main(String[] args) throws Exception {
        // Parse command line arguments.
        int port = 8080;
        boolean ssl = false;
        for (String arg : args) {
            if (arg.equals("--ssl")) {
                ssl = true;
            } else {
                port = Integer.parseInt(arg);
            }
        }

// Start the server.
        CraftSocketServer server = new CraftSocketServer(port, ssl);
        server.run();
        LOGGER.info("CraftSocketServer started on port: {}", server.getPort());
    }
}