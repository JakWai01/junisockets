package space.nebulark.junisockets;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/endpoint")
public class SignalingServer {

    private String host;
    private int number; 

    public SignalingServer(String host, int number) {
        this.host = host;
        this.number = number;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get Session and WebSocket connection
        System.out.println("onOpen::" + session.getId());
    }

    @OnMessage 
    public void onMessage(Session session, Message message) throws IOException {
        // Handle new Message
        System.out.println("onMessage::From=" + session.getId() + " Message=" + message);

        try {
            session.getBasicRemote().sendText("Hello Client " + session.getId() + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        System.out.println("onClose::" + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        // Do error handling here
        System.out.println("onError::" + throwable.getMessage());
    }
    
}
