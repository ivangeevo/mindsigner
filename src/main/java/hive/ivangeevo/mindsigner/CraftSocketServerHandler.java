package hive.ivangeevo.mindsigner;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class CraftSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            // Handle HTTP request.
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            // Handle WebSocket frame.
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.writeAndFlush(new TextWebSocketFrame("Welcome to the CraftSocketServer!"));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Print farewell message when client disconnects.
        System.out.println("WebSocket Client disconnected: " + ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handshake the WebSocket connection.
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketURL(req), null, true, 65536);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            // Handle text frame.
            String command = ((TextWebSocketFrame) frame).text();
            String result = executeCommand(command);
            ctx.writeAndFlush(new TextWebSocketFrame(result));
        } else {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
    }

    private String executeCommand(String command) {
        // Execute the command and return the result.
        // Replace this with your own implementation.
        return "Command executed: " + command;
    }

    private String getWebSocketURL(FullHttpRequest req) {
        String protocol = "ws";
        if (req.headers().contains("X-Forwarded-Proto") && req.headers().get("X-Forwarded-Proto").equalsIgnoreCase("https")) {
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get("Host") + req.uri();
    }
}

