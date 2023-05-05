package hive.ivangeevo.mindsigner;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLEngine;
import java.security.cert.X509Certificate;

public class CraftSocketServer {

    private static final int PORT = 8080;
    private static final String KEYSTORE_PATH = "./craftsocketclient/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String TRUSTSTORE_PATH = "./craftsocketclient/truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "password";

    public CraftSocketServer(int port, boolean b) {
    }

    public static void main(String[] args) throws Exception {

        // Generate a self-signed certificate
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

        // Create the server bootstrap
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // Configure the server bootstrap
        serverBootstrap.channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        // Add SSL handler to pipeline
                        SSLEngine sslEngine = sslCtx.newEngine(ch.alloc());
                        pipeline.addLast(new SslHandler(sslEngine));

                        // Add HTTP codecs to pipeline
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));

                        // Add WebSocket server protocol handler to pipeline
                        pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));

                        // Add custom handler to pipeline
                        char[] keystorePassword = KEYSTORE_PASSWORD.toCharArray();
                        X509Certificate[] truststore = CraftSocketServerHandler.loadCertificates(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD);
                        WebSocketServerHandshakerFactory wsFactory = null;
                        pipeline.addLast(new CraftSocketServerHandler(keystorePassword, truststore, null));
                    }
                });

        // Start the server
        serverBootstrap.bind(PORT).sync().channel().closeFuture().sync();
    }

    public void run() {
    }
}
