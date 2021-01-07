package space.nebulark.junisockets.services;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import space.nebulark.junisockets.addresses.IPAddress;
import space.nebulark.junisockets.addresses.TCPAddress;
import space.nebulark.junisockets.errors.ClientClosed;
import space.nebulark.junisockets.errors.ClientDoesNotExist;
import space.nebulark.junisockets.errors.PortAlreadyAllocated;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;
import space.nebulark.junisockets.errors.UnimplementedOperation;
import space.nebulark.junisockets.models.MAlias;
import space.nebulark.junisockets.operations.Alias;
import space.nebulark.junisockets.operations.ESignalingOperationCode;
import space.nebulark.junisockets.operations.Goodbye;
import space.nebulark.junisockets.operations.OperationFactory;

/**
 * SignalingServer
 */
public class SignalingServer extends WebSocketServer {

    private Logger logger = Logger.getLogger(SignalingServer.class);
    public ConcurrentHashMap<String, HashMap<Integer, List<Integer>>> subnets = new ConcurrentHashMap<String, HashMap<Integer, List<Integer>>>();
    private ReentrantLock mutex = new ReentrantLock();
    public ConcurrentHashMap<String, WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();
    public ConcurrentHashMap<String, MAlias> aliases = new ConcurrentHashMap<String, MAlias>();
    private boolean isOpen = false;
    private IPAddress ip = new IPAddress(logger, mutex, subnets);
    private TCPAddress tcpAddress = new TCPAddress(logger, mutex, subnets, ip);
    private ServerOperation op = new ServerOperation(clients, aliases, ip, tcpAddress, logger);

    /**
     * Constructor SignalingServer
     * @param logger logger
     * @param address address
     */
    public SignalingServer(Logger logger, InetSocketAddress address) {
        super(address);
        setReuseAddr(true);

        this.logger = logger;
    }

    
    /** 
     * Called after an opening handshake has been performed and the given websocket is ready to be written on.
     * @param conn conn
     * @param handshake handshake
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        isOpen = true;

        logger.debug("Opening signaling server");
    }

    
    /** 
     * Called after the websocket connection has been closed.
     * @param conn conn
     * @param code code
     * @param reason reason
     * @param remote remote
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String id = "";

        // Get the current key of the connection in the client map
        for (int i = 0; i < clients.size(); i++) {
            String currentKey = (String) clients.keySet().toArray()[i];

            if (clients.get(currentKey) == conn) {
                id = currentKey;
            }
        }

        logger.debug("Registering goodbye " + id);

        if (clients.containsKey(id)) {
            // If clients contains that id, remove it from clients
            clients.remove(id);

            final String targetId = id;

            aliases.forEach((clientId, alias) -> {
                // Check if id of alias matches the targetId
                if (alias.getId().equals(targetId)) {
                    // If id of alias matches the targetId remove current alias and remove TCP address
                    aliases.remove(clientId);
                    tcpAddress.removeTCPAddress(clientId);

                    // Send each client an alias that the targetId is not set anymore
                    clients.forEach((key, client) -> {

                        try {
                            op.send(clients.get(key), (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId(targetId).setAlias(clientId).setSet(false).getOperation());
                        } catch (ClientClosed e1) {
                            logger.error(e1);
                        }

                        logger.debug("Sent alias " + targetId + key);
                    });
                }
            });

            // Remove IP address of targetId
            ip.removeIPAddress(tcpAddress.parseTCPAddress(targetId)[0]);

            // Broadcast Goodbye to all remaining clients
            op.send((Goodbye) new OperationFactory(ESignalingOperationCode.GOODBYE).setId(targetId).getOperation());

            logger.debug("Sent goodbye " + targetId);
        } else {
            try {
                throw new ClientDoesNotExist();
            } catch (ClientDoesNotExist e1) {
                logger.error(e1);
            }
        }

        logger.debug("Client disconnected " + id);

        isOpen = false;
    }

    
    /** 
     * Callback for string messages received from the remote host
     * @param conn conn
     * @param message message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {

        JSONParser parser = new JSONParser();
        Object jsonObj = null;

        try {
            jsonObj = parser.parse(message);
        } catch (ParseException e) {
            logger.error(e);
        }

        JSONObject operation = (JSONObject) jsonObj;

        try {
            // Handle the incoming operation
            handleOperation(operation, conn);
        } catch (UnimplementedOperation e) {
            logger.error(e);
        }

    }

    
    /** 
     * Called when errors occurs. If an error causes the websocket connection to fail onClose(WebSocket, int, String, boolean) will be called additionally. This method will be called primarily because of IO or protocol errors. If the given exception is an RuntimeException that probably means that you encountered a bug.
     * @param conn conn
     * @param ex exception
     */
    @Override
    public void onError(WebSocket conn, Exception e) {
       logger.error(e); 
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

        Thread thread = new Thread(() -> {
            try {
                // Call ping in seperate thread
                ping();
            } catch (InterruptedException e) {
                logger.error(e);
            }
        });

        thread.start();
    }

    
    /** 
     * Handling the incoming operation by calling the right handler.
     * @param operation operation
     * @param conn conn
     * @throws UnimplementedOperation Thrown if an unimplemented operation was received
     */
    private void handleOperation(JSONObject operation, WebSocket conn) throws UnimplementedOperation {

        logger.debug("Handling operation: " + operation + conn);

        // Check which operations was send by the client and call the corresponding handler
        if (operation.get("opcode").equals(ESignalingOperationCode.KNOCK.getValue())) {

            logger.trace("Received knock");

            Thread thread = new Thread(() -> {
                op.handleKnock((JSONObject) operation.get("data"), conn);
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.OFFER.getValue())) {

            logger.trace("Received offer");

            Thread thread = new Thread(() -> {
                op.handleOffer((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.ANSWER.getValue())) {

            logger.trace("Received answer");

            Thread thread = new Thread(() -> {
                op.handleAnswer((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.CANDIDATE.getValue())) {

            logger.trace("Received candidate");

            Thread thread = new Thread(() -> {
                op.handleCandidate((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.BIND.getValue())) {

            logger.trace("Received bind");

            Thread thread = new Thread(() -> {
                try {
                    op.handleBind((JSONObject) operation.get("data"));
                } catch (PortAlreadyAllocated e) {
                    logger.error(e);
                } catch (SubnetDoesNotExist e) {
                    logger.error(e);
                }
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.ACCEPTING.getValue())) {

            logger.trace("Received accepting");

            Thread thread = new Thread(() -> {
                op.handleAccepting((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.SHUTDOWN.getValue())) {

            logger.trace("Received shutdown");

            Thread thread = new Thread(() -> {
                op.handleShutdown((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.CONNECT.getValue())) {
            logger.trace("Received connect");

            Thread thread = new Thread(() -> {
                try {
                    op.handleConnect((JSONObject) operation.get("data"));
                } catch (SuffixDoesNotExist e) {
                    logger.error(e);
                } catch (SubnetDoesNotExist e) {
                    logger.error(e);
                }
            });
            thread.start();
        } else {
            throw new UnimplementedOperation(operation.get("opcode"));
        }
    }

    
    /** 
     * Send a ping to the other end
     * @throws InterruptedException Thrown if interrupted
     */
    private void ping() throws InterruptedException {

        while (isOpen == true) {

            // Send ping to all clients every 30 seconds
            for (int i = 0; i < clients.size(); i++) {
                String key = (String) clients.keySet().toArray()[i];

                try {
                    clients.get(key).sendPing();
                } catch (WebsocketNotConnectedException e) {
                    logger.error(e);
                    //e.printStackTrace();
                }
            }
            logger.debug("Sent ping!");
            Thread.sleep(30000);

        }
    }
}