package space.nebulark.junisockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

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
    public void onMessage(WebSocket conn, String message) {
        broadcast(message);
        System.out.println(conn + ": " + message);

        Thread newThread = new Thread(() -> {
        handleOperation(message);
        });
        newThread.start();


    }

    // (operation: ISignalingOperation<TSignalingData>, client: WebSocket)
    private static void handleOperation(String message) {

        System.out.println("Handling Operation");
        // equals
        if (message.equals(ESignalingOperationCode.KNOCK.getValue())) {

            logger.debug("Received knock");

            Thread thread = new Thread(() -> {
                handleKnock();
            });
            thread.start();
        } else if (message.equals(ESignalingOperationCode.OFFER.getValue())) {

            logger.debug("Received offer");

            Thread thread = new Thread(() -> {
                handleOffer();
            });
            thread.start();
        } else if (message.equals(ESignalingOperationCode.ANSWER.getValue())) {

            logger.debug("Received answer");

            Thread thread = new Thread(() -> {
                handleAnswer();
            });
            thread.start();
        } else if (message.equals(ESignalingOperationCode.CANDIDATE.getValue())) {

            logger.debug("Received candidate");

            Thread thread = new Thread(() -> {
                handleCandidate();
            });
            thread.start();
        } else if (message.equals(ESignalingOperationCode.BIND.getValue())) {    
        
            logger.debug("Received bind");

            Thread thread = new Thread(() -> {
                handleBind();
            });
            thread.start();
        } else if (message.equals(ESignalingOperationCode.ACCEPTING.getValue())) {

            logger.debug("Received accepting");

            Thread thread = new Thread(() -> {
                handleAccepting();
            });
            thread.start();
        } else if (message.equals(ESignalingOperationCode.SHUTDOWN.getValue())) {

            logger.debug("Received shutdown");

            Thread thread = new Thread(() -> {
                handleShutdown();
            });
            thread.start();
        } else if (message.equals(ESignalingOperationCode.CONNECT.getValue())) {
            logger.debug("Received connect");

            Thread thread = new Thread(() -> {
                handleConnect();
            });
            thread.start();
        } else {
            // Custom error messsage
            System.out.println("None of the above");
            logger.debug("None of the above");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific
            // websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    private static void handleKnock() {
        // check for right debug level, add data
        logger.debug("Handling knock");

    }
    public static void handleOffer() {
        // check for right debug level, add data
        logger.debug("Handling offer");

    }

    private static void handleAnswer() {
        // check for right debug level, add data
        logger.debug("Handling answer");

    }

    private static void handleCandidate() {
        // check for right debug level, add data
        logger.debug("Handling candidate");

    }

    private static void handleBind() {
        // check for right debug level, add data
        logger.debug("Handling bind");

    }

    private static void handleAccepting() {
        // check for right debug level, add data
        logger.debug("Handling accepting");

    }

    private static void handleShutdown() {
        // check for right debug level, add data
        logger.debug("Handling shutdown");

    }

    private static void handleConnect() {
        // check for right debug leve, add data
        logger.debug("Handling connect");

    }

    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 8891;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        SignalingServer s = new SignalingServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            s.broadcast(in);
            if (in.equals("exit")) {
                s.stop(1000);
                break;
            }
        }
    }
}
