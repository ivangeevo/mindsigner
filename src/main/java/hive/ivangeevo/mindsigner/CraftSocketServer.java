package hive.ivangeevo.mindsigner;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

import io.netty.handler.codec.http.HttpRequest;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;

import static com.google.common.net.HttpHeaders.HOST;
import static io.netty.handler.codec.rtsp.RtspHeaderValues.PORT;

public class CraftSocketServer implements Runnable {
    private final int port;

    public CraftSocketServer(int port) {
        this.port = port;
    }

    public CraftSocketServer(int port, Object o) {
    }

    @Override
    public void run() {
        // Load keystore
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        try (FileInputStream fis = new FileInputStream("./craftsocketclient/keystore.p12")) {
            ks.load(fis, "password".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        KeyManagerFactory kmf;
        try {
            kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, "password".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Load truststore
        KeyStore ts = null;
        try {
            ts = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        try (FileInputStream fis = new FileInputStream("./craftsocketclient/truststore.jks")) {
            ts.load(fis, "password".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ts);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Create SSL context
        SslContext sslCtx = null;
        try {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
// Handle SSL context creation failure
            e.printStackTrace();
        }

// Set up Netty client bootstrap
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
// Add SSL handler
                            p.addLast(sslCtx.newHandler(ch.alloc()));
// Add custom message encoder and decoder
                            p.addLast(new CustomMessageEncoder());
                            p.addLast(new CustomMessageDecoder());
// Add business logic handler
                            p.addLast(new CustomClientHandler());
                        }
                    });
            // Start the client
            ChannelFuture f = b.connect().sync();

// Wait until the connection is closed
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
// Handle client startup/shutdown interruption
            e.printStackTrace();
        } finally {
// Shut down the event loop group
            group.shutdownGracefully();
        }
    }
}