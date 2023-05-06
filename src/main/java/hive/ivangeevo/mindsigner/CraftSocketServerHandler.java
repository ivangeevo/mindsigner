package hive.ivangeevo.mindsigner;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CraftSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = Logger.getLogger(WebSocketServerProtocolHandler.class.getName());

    private final WebSocketServerHandshaker handshaker;
    private final SslContext sslCtx;

    public CraftSocketServerHandler(char[] keystorePassword, WebSocketServerHandshaker handshaker, SslContext sslCtx) {
        this.handshaker = handshaker;
        this.sslCtx = sslCtx;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send a welcome message when a new client connects
        String message = "Welcome to the WebSocket server!";
        WebSocketFrame frame = new TextWebSocketFrame(message);
        ctx.writeAndFlush(frame);

        logger.info("Client connected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // Handle incoming WebSocket frames
        if (frame instanceof TextWebSocketFrame) {
            // Handle text frames
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            String message = textFrame.text();
            logger.info("Received message from client: " + message);

            // Echo back the message to the client
            String echoMessage = "You said: " + message;
            WebSocketFrame echoFrame = new TextWebSocketFrame(echoMessage);
            ctx.writeAndFlush(echoFrame);
        } else {
            logger.warning("Unsupported frame type: " + frame.getClass().getName());
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(Level.WARNING, "Exception caught", cause);
        ctx.close();
    }
}
