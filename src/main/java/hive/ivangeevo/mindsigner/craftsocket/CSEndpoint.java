package hive.ivangeevo.mindsigner.craftsocket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/CraftSocketEndpoint")
public class CSEndpoint {

    private final CSWebsocketServer craftSocketServer;



    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    public CSEndpoint(CSWebsocketServer craftSocketServer) {
        this.craftSocketServer = craftSocketServer;
    }

    @OnOpen
    public void onOpen(Session session) {

        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {

        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Handle errors
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        craftSocketServer.handleMessage(message);
    }

    public void setCraftSocketServer(CSWebsocketServer craftSocketServer) {
        // Implement this method
    }


    static void sendToAllConnectedSessions(String message) throws IOException {
        for (Session session : sessions) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        }
    }
}
