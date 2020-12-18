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

import space.nebulark.junisockets.operations.ESignalingOperationCode;

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
            // handleOperation
        });
        newThread.start();
    }

    // (operation: ISignalingOperation<TSignalingData>, client: WebSocket)
    private void handleOperation(String message) {

        if (message == ESignalingOperationCode.KNOCK.getValue()) {

            logger.debug("Received knock");

            Thread thread = new Thread(() -> {
                handleKnock();
            });
            thread.start();
        } else if (message == ESignalingOperationCode.OFFER.getValue()) {

            logger.debug("Received offer");

            Thread thread = new Thread(() -> {
                handleOffer();
            });
            thread.start();
        } else if (message == ESignalingOperationCode.ANSWER.getValue()) {

            logger.debug("Received answer");

            Thread thread = new Thread(() -> {
                handleAnswer();
            });
            thread.start();
        } else if (message == ESignalingOperationCode.CANDIDATE.getValue()) {

            logger.debug("Received candidate");

            Thread thread = new Thread(() -> {
                handleCandidate();
            });
            thread.start();
        } else if (message == ESignalingOperationCode.BIND.getValue()) {

            logger.debug("Received bind");

            Thread thread = new Thread(() -> {
                handleBind();
            });
            thread.start();
        } else if (message == ESignalingOperationCode.ACCEPTING.getValue()) {

            logger.debug("Received accepting");

            Thread thread = new Thread(() -> {
                handleAccepting();
            });
            thread.start();
        } else if (message == ESignalingOperationCode.SHUTDOWN.getValue()) {

            logger.debug("Received shutdown");

            Thread thread = new Thread(() -> {
                handleShutdown();
            });
            thread.start();
        } else if (message == ESignalingOperationCode.CONNECT.getValue()) {

            logger.debug("Received connect");

            Thread thread = new Thread(() -> {
                handleConnect();
            });
            thread.start();
        } else {
            // Custom error messsage
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

    private void handleKnock() {
        // check for right debug level, add data
        logger.debug("Handling knock");

    }

    private void handleOffer() {
        // check for right debug level, add data
        logger.debug("Handling offer");

    }

    private void handleAnswer() {
        // check for right debug level, add data
        logger.debug("Handling answer");

    }

    private void handleCandidate() {
        // check for right debug level, add data
        logger.debug("Handling candidate");

    }

    private void handleBind() {
        // check for right debug level, add data
        logger.debug("Handling bind");

    }

    private void handleAccepting() {
        // check for right debug level, add data
        logger.debug("Handling accepting");

    }

    private void handleShutdown() {
        // check for right debug level, add data
        logger.debug("Handling shutdown");

    }

    private void handleConnect() {
        // check for right debug leve, add data
        logger.debug("Handling connect");

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
