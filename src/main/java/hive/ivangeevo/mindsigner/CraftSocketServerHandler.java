package hive.ivangeevo.mindsigner;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class CraftSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = Logger.getLogger(WebSocketServerProtocolHandler.class.getName());

    private final WebSocketServerHandshaker handshaker;
    private final SslContext sslCtx;

    public CraftSocketServerHandler(char[] keystorePassword, WebSocketServerHandshaker handshaker, SslContext sslCtx) {
        this.handshaker = handshaker;
        this.sslCtx = sslCtx;
    }

    // No-argument constructor
    public CraftSocketServerHandler() {
        this.handshaker = null;
        this.sslCtx = null;
    }

    public CraftSocketServerHandler(WebSocketServerHandshaker handshaker, SslContext sslCtx) {
        this.handshaker = handshaker;

        this.sslCtx = sslCtx;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Send a welcome message when a new client connects
        String message = "Welcome to the WebSocket server!";
        WebSocketFrame frame = new TextWebSocketFrame(message);
        ctx.writeAndFlush(frame);

        logger.info("Client connected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)  {
        logger.info("Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame)  {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        String message = ((TextWebSocketFrame) frame).text();
        logger.info(String.format("%s received %s", ctx.channel(), message));
        ctx.channel().write(new TextWebSocketFrame("Server received: " + message));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        logger.log(Level.WARNING, "Exception caught", cause);
        ctx.close();
    }
}