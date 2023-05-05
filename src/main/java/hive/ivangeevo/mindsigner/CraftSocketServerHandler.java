package hive.ivangeevo.mindsigner;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.PrintStream;
import java.security.cert.X509Certificate;

import static io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.ServerHandshakeStateEvent;

public class CraftSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketServerHandshakerFactory wsFactory;
    private WebSocketServerHandshaker handshaker;
    private static final String WEBSOCKET_PATH = "/websocket";
    private SslContext sslCtx;

    public CraftSocketServerHandler(WebSocketServerHandshakerFactory wsFactory, SslContext sslCtx) {
        this.wsFactory = wsFactory;
        this.sslCtx = sslCtx;
    }

    public CraftSocketServerHandler(char[] keystorePassword, X509Certificate[] truststore, WebSocketServerHandshakerFactory wsFactory) {
        this.wsFactory = wsFactory;
    }

    public static X509Certificate[] loadCertificates(String truststorePath, String truststorePassword) {
        return new X509Certificate[0];
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Client " + incoming.remoteAddress() + " connected");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation((HttpRequest) msg),
                    null,  // subprotocols (null means no subprotocols)
                    true   // allow extensions
            );

            handshaker = wsFactory.newHandshaker((HttpRequest) msg);
            if (msg instanceof FullHttpRequest) {
                handshaker.handshake(ctx.channel(), (FullHttpRequest) msg);
            } else {
                handshaker.handshake(ctx.channel(), (HttpRequest) msg);
            }
        }
    }

    private String getWebSocketLocation(HttpRequest msg) {
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("Reader idle, closing channel");
                ctx.close();
            }
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        // Ping frame
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // Send the response back.
        String request = ((TextWebSocketFrame) frame).text();
        PrintStream out = System.out;
    }
}