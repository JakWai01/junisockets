package space.nebulark.junisockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SignalingServer extends WebSocketServer implements ISignalingService {

    private static HashMap<String, WebSocket> clients = new HashMap<String, WebSocket>();
    //private HashMap<String, MAlias> aliases = new HashMap<String, MAlias>();
    
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
            
        JSONParser parser = new JSONParser();
        
        Object jsonObj = null;
        
        try {
			jsonObj = parser.parse(message);
		} catch (ParseException e) {
			e.printStackTrace();
		}

        JSONObject operation = (JSONObject) jsonObj;

        handleOperation(operation, conn);

    }

    private static void handleOperation(JSONObject operation, WebSocket conn) {

        logger.trace("Handling operation: " + operation + conn);

        if (operation.get("opcode").equals(ESignalingOperationCode.KNOCK.getValue())) {

            logger.debug("Received knock");
            System.out.println("Received knock");

            Thread thread = new Thread(() -> {
                handleKnock((JSONObject)operation.get("data"), conn);
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.OFFER.getValue())) {

            logger.debug("Received offer");

            Thread thread = new Thread(() -> {
                handleOffer((JSONObject)operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.ANSWER.getValue())) {

            logger.debug("Received answer");

            Thread thread = new Thread(() -> {
                handleAnswer();
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.CANDIDATE.getValue())) {

            logger.debug("Received candidate");

            Thread thread = new Thread(() -> {
                handleCandidate();
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.BIND.getValue())) {    
        
            logger.debug("Received bind");

            Thread thread = new Thread(() -> {
                handleBind();
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.ACCEPTING.getValue())) {

            logger.debug("Received accepting");

            Thread thread = new Thread(() -> {
                handleAccepting();
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.SHUTDOWN.getValue())) {

            logger.debug("Received shutdown");

            Thread thread = new Thread(() -> {
                handleShutdown();
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.CONNECT.getValue())) {
            logger.debug("Received connect");

            Thread thread = new Thread(() -> {
                handleConnect();
            });
            thread.start();
        } else {
            // Custom error messsage instead of error
            logger.fatal("Unimplemented Operation");
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

    private static void handleKnock(JSONObject data, WebSocket conn) {
        
        logger.trace("Handling knock");

        String subnet = (String)data.get("subnet");

        final String id = createIPAddress(subnet);

        System.out.println(id);

        if (id != "-1") {
            send(conn, new Acknowledgement(id, false));
        } else {
            send(conn, new Acknowledgement(id, true));
            logger.debug("Knock rejected " + "{" + id + ", reason: subnet overflow}");

            return;
        }

    }
    public static void handleOffer(JSONObject data) {
        logger.trace("Handling offer: " + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            send(client, new Offer((String)data.get("offererId"), (String)data.get("answererId"), (String)data.get("offer")));
            logger.debug("Sent offer" + data.get("offererId") + data.get("answererId") + data.get("offer"));
        });
        thread.start();

    }

    private static void handleAnswer(JSONObject data) {
        logger.trace("Handling answer: " + data);

        final WebSocket client = clients.get(data.get("offererId"));

        send(client, new Answer((String)data.get("offererId"), (String)data.get("answererId"), (String)data.get("answer")));

        logger.debug("Send answer" + data);
    }

    private static void handleCandidate(JSONObject data) {
        logger.trace("Handling candidate" + data);

        final WebSocket client = clients.get(data.get("answererId"));

        send(client, new Candidate((String)data.get("offererId"), (String)data.get("answererId"), (String)data.get("candidate")));

        logger.debug("Sent candidate" + data);
    }

    private static void handleBind() {
        logger.trace("Handling bind");

    }

    private static void handleAccepting() {
        logger.trace("Handling accepting");

    }

    private static void handleShutdown() {
        logger.trace("Handling shutdown");

    }

    private static void handleConnect() {
        logger.trace("Handling connect");

    }

    private static HashMap<String, HashMap<Integer, Integer[]>> subnets = new HashMap<String, HashMap<Integer, Integer[]>>();

    private static String createIPAddress(String subnet) {
        logger.trace("Creating IP address" + subnet);

        try {
            if (!subnets.containsKey(subnet)) {
                subnets.put(subnet, new HashMap<Integer, Integer[]>());
            }
        
            List<Integer> existingMembersSorted = subnets.get(subnet).keySet().stream().collect(Collectors.toList());

            Collections.sort(existingMembersSorted, (o1, o2) -> o1.compareTo(o2)); 
        
            boolean foundSuffix = false;
            int newSuffix = 0;
           
            for (int i = 0; i < existingMembersSorted.size(); i++) { 

                if (i != existingMembersSorted.get(i)) {
                    newSuffix = i;
                    foundSuffix = true;       
                }
            }

            if (!foundSuffix) {
                newSuffix = existingMembersSorted.size(); 
            }

            if (newSuffix > 255) {
                return "-1";
            }

            Integer[] newMember = new Integer[0];

            subnets.get(subnet).put(newSuffix, newMember); // We ensure above

            return toIPAddress(subnet, newSuffix);

        } finally {
            // release lock (mutex)
        }
    }

    private void createTCPAddress(String ipAddress) {
        logger.trace("Creating TCP address" + ipAddress);
    }

    private void claimTCPAddress(String tcpAddress) {
        logger.trace("Claiming TCP address" + tcpAddress);
    }

    private void removeIPAddress(String ipAddress) {
        logger.trace("Removing IP address" + ipAddress);
    }

    private void removeTCPAddress(String tcpAddress) {
        logger.trace("Removing TCP address" + tcpAddress);
    }

    private static String toIPAddress(String subnet, int suffix) {
        logger.trace("Converting to IP address" + subnet + suffix);

        String ipAddress = subnet + "." + suffix;

        return ipAddress;
    }

    private void toTCPAddress(String ipAddress, int port) {
        logger.trace("Converting to TCP address" + ipAddress + port);
    }   
    
    private void parseIPAddress(String ipAddress) {
        logger.trace("Parsing IP address" + ipAddress);
    }

    private void parseTCPAddress(String tcpAddress) {
        logger.trace("Parsing TCP address" + tcpAddress);
    }

    private static void send(WebSocket conn, Acknowledgement operation) {
        // Probably change this debug information to json
        logger.debug("Sending" + operation); 
        
        if (conn != null) { 
            conn.send("{\"id\":\"" + operation.getId() + "\", \"rejected\":" + operation.getRejected() + "}");
        } else {
            // create new ClientClosedError() custom exception
            logger.fatal("Client closed");
        }
    }

    private static void send(WebSocket conn, Offer operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {
            conn.send("{\"offererId\":\"" + operation.getOffererId()  + "\", \"answererId\":" + operation.getAnswererId() + "\", \"offer\":" + operation.getOffer() + "}");
        } else {

            logger.fatal("Client closed");
        }
    }

    public static void send(WebSocket conn, Answer operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {
            conn.send("{\"offererId\":\"" + operation.getOffererId()  + "\", \"answererId\":" + operation.getAnswererId() + "\", \"answer\":" + operation.getAnswer() + "}");
        } else {

            logger.fatal("Client closed");
        }
    }

    public static void send(WebSocket conn, Candidate operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {
            conn.send("{\"offererId\":\"" + operation.getOffererId()  + "\", \"answererId\":" + operation.getAnswererId() + "\", \"candidate\":" + operation.getCandidate() + "}");
        } else {

            logger.fatal("Client closed");
        }

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
