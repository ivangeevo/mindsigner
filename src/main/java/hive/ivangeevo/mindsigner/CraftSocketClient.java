import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class CraftSocketClient extends WebSocketClient {

    public CraftSocketClient(String serverAddress) throws URISyntaxException {
        super(new URI("ws://" + serverAddress + ":8080"));
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket connected");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed with code " + code + " and reason " + reason);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message from server: " + message);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WebSocket error: " + ex.getMessage());
    }
}
