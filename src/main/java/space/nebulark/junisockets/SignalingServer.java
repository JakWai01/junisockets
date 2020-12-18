package space.nebulark.junisockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import space.nebulark.junisockets.operations.ESIGNALING_OPCODES;

public class SignalingServer extends WebSocketServer {

   final static Logger logger = Logger.getLogger(SignalingServer.class);

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
        logger.debug("Opening signaling server");

        conn.send("Welcome to the server!");
        broadcast("new connection: " + handshake.getResourceDescriptor());
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + "entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.debug("Shutting down signaling server");

        broadcast(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn,  String message) {
        broadcast(message);
        System.out.println(conn + ": " + message);

        Thread newThread = new Thread(() -> {
            // handleOperation
        });
        newThread.start();
    }
    

    // (operation: ISignalingOperation<TSignalingData>, client: WebSocket)
    private void handleOperation(ESIGNALING_OPCODES operation) {
    
        switch (operation) {
            case KNOCK: {
                
                // log add data
                logger.debug("Received knock");
                

                // call handleknock
                Thread newThread = new Thread(() -> {
                    // handleKnock();
                });
                newThread.start();
            
                break;
            }

            case OFFER: {

                // log add data
                logger.debug("Received offer");

                Thread newThread = new Thread(() -> {
                    // handleOffer();
                });
                newThread.start();

                break;
            }

            case ANSWER: {

                // log add data
                logger.debug("Received answer");

                Thread newThread = new Thread(() -> {

                });
                newThread.start();

                break;
            }

            case CANDIDATE: {
                
                // log add data
                logger.debug("Received candidate");

                Thread newThread = new Thread(() -> {

                });
                newThread.start();

                break;
            }

            case BIND: {

                // log add data
                logger.debug("Received bind");

                Thread newThread = new Thread(() -> {

                });
                newThread.start();

                break;
            }

            case ACCEPTING: {
                
                // log add data
                logger.debug("Received accepting");

                Thread newThread = new Thread(() -> {

                });
                newThread.start();

                break;
            }

            case SHUTDOWN: {

                // log add data
                logger.debug("Received shutdown");

                Thread newThread = new Thread(() -> {

                });
                newThread.start();

                break;
            }

            case CONNECT: {

                // log add data
                logger.debug("Received conncet");

                Thread newThread = new Thread(() -> {

                });
                newThread.start();

                break;
            }

            default: {
                
                // throw custom exception
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

    private void handleKnock(IKnockData data, WebSocket conn) {

    }

    private void handleOffer(IOfferData data) {
        
    }

    private void handleAnswer(IAnswerData data) {

    }

    private void handleCandidate(ICandidateData data) {

    }

    private void handleBind(IBindData data) {

    }

    private void handleAccepting(IAcceptData data) {

    }

    private void handleShutdown(IShutdownData data) {

    }

    private void handleConnect(IConnectData data) {
        
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
