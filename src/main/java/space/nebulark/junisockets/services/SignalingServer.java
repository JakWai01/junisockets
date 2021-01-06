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
        for (int i = 0; i < clients.size(); i++) {
            String currentKey = (String) clients.keySet().toArray()[i];

            if (clients.get(currentKey) == conn) {
                id = currentKey;
            }
        }

        logger.debug("Registering goodbye " + id);

        if (clients.containsKey(id)) {

            clients.remove(id);

            final String targetId = id;

            aliases.forEach((clientId, alias) -> {
                if (alias.getId().equals(targetId)) {
                    aliases.remove(clientId);
                    logger.debug("after ip");
                    tcpAddress.removeTCPAddress(clientId);

                    clients.forEach((key, client) -> {

                        try {
                            op.send(clients.get(key), (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId(targetId).setAlias(clientId).setSet(false).getOperation());
                        } catch (ClientClosed e1) {
                            logger.fatal("Client closed");
                            e1.printStackTrace();
                        }

                        logger.debug("Sent alias " + targetId + key);
                    });
                }
            });

    
            ip.removeIPAddress(tcpAddress.parseTCPAddress(id)[0]);

            op.send((Goodbye) new OperationFactory(ESignalingOperationCode.GOODBYE).setId(targetId).getOperation());

            logger.debug("Sent goodbye " + targetId);
        } else {
            try {
                throw new ClientDoesNotExist();
            } catch (ClientDoesNotExist e1) {
                logger.fatal("Client does not exist");
                e1.printStackTrace();
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
            logger.fatal("Parse exception");
            e.printStackTrace();
        }

        JSONObject operation = (JSONObject) jsonObj;

        try {
            handleOperation(operation, conn);
        } catch (UnimplementedOperation e) {
            logger.fatal("Unimplemented operation: " + operation);
            e.printStackTrace();
        }

    }

    
    /** 
     * Called when errors occurs. If an error causes the websocket connection to fail onClose(WebSocket, int, String, boolean) will be called additionally. This method will be called primarily because of IO or protocol errors. If the given exception is an RuntimeException that probably means that you encountered a bug.
     * @param conn conn
     * @param ex exception
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

        Thread thread = new Thread(() -> {
            try {
                ping();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                    logger.fatal("Port already allocated");
                    e.printStackTrace();
                } catch (SubnetDoesNotExist e) {
                    logger.fatal("Subnet does not exist");
                    e.printStackTrace();
                }
                ;
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
                    logger.fatal("Suffix does not exist");
                    e.printStackTrace();
                } catch (SubnetDoesNotExist e) {
                    logger.fatal("Suffix does not exist");
                    e.printStackTrace();
                }
                ;
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

            for (int i = 0; i < clients.size(); i++) {
                String key = (String) clients.keySet().toArray()[i];

                try {
                    clients.get(key).sendPing();
                } catch (WebsocketNotConnectedException e) {
                    logger.fatal("WebSocket not connected");
                    e.printStackTrace();
                }
            }
            logger.debug("Sent ping!");
            Thread.sleep(30000);

        }
    }
}