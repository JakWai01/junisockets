package space.nebulark.junisockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import space.nebulark.junisockets.operations.ESIGNALING_OPCODES;

public class SignalingServer extends WebSocketServer {

    public SignalingServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public SignalingServer(InetSocketAddress address) {
        super(address);
    }

    public SignalingServer(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!");
        broadcast("new connection: " + handshake.getResourceDescriptor());
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + "entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        broadcast(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn,  String message) {
        broadcast(message);
        System.out.println(conn + ": " + message);

        // handleOperation will be called here
    }
    

    // (operation: ISignalingOperation<TSignalingData>, client: WebSocket)
    private void handleOperation(ESIGNALING_OPCODES operation) {
    
        switch (operation) {
            case KNOCK: {

            }

            case OFFER: {

            }

            case ANSWER: {

            }

            case CANDIDATE: {

            }

            case BIND: {

            }

            case ACCEPTING: {

            }

            case SHUTDOWN: {

            }

            case CONNECT: {

            }

            default: {

            }
        }
    }

    @Override 
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
                  // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    
  public static void main(String[] args) throws InterruptedException, IOException {
    int port = 8887; 
    try {
      port = Integer.parseInt(args[0]);
    } catch (Exception ex) {
    }
    ChatServer s = new ChatServer(port);
    s.start();
    System.out.println("ChatServer started on port: " + s.getPort());
  }
}
