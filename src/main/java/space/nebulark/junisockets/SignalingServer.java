package space.nebulark.junisockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
        //Object operation = null; 
        //send(conn, operation);
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
            
        // This needs to be a jsonobject
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

    // (operation: ISignalingOperation<TSignalingData>, client: WebSocket)
    private static void handleOperation(JSONObject operation, WebSocket conn) {

        System.out.println("Handling Operation");
        // equals
        if (operation.get("opcode").equals(ESignalingOperationCode.KNOCK.getValue())) {

            logger.debug("Received knock");
            System.out.println("Received knock");
            // Bis hier funktioniert alles


            // Think about that again
            //Knock knock =  new Knock((IKnockData)operation.get("data"));
            
            //Mapper mapper = DozerBeanMapperBuilder.buildDefault();

            //Knock knock = mapper.map(operation, Knock.class);

            System.out.println(operation.get("data"));
        
            //System.out.println(knock.data.subnet);

            Thread thread = new Thread(() -> {
                handleKnock(operation.get("data"), conn);
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.OFFER.getValue())) {

            logger.debug("Received offer");

            Thread thread = new Thread(() -> {
                handleOffer();
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

    private static void handleKnock(Object data, WebSocket conn) {
        // check for right debug level, add data
        logger.debug("Handling knock");

        System.out.println("data: " + data);
        System.out.println("conn: " + conn);

        // const id = await this.createIPAdress(data.subnet);

        // send something back
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

    // Create MMember instead of Integer[]
    private HashMap<String, HashMap<Integer, Integer[]>> subnets = new HashMap<String, HashMap<Integer, Integer[]>>();

    private void createIPAddress(String subnet) {
        logger.trace("Creating IP address" + subnet);

        try {
            if (!this.subnets.containsKey(subnet)) {
                // ensure that put is the right method and we do not need to provide actual values in the HashMap
                this.subnets.put(subnet, new HashMap());
            }
        
            final Set<Integer> existingMembers = subnets.get(subnet).keySet();

            List<Integer> existingMembersSorted = existingMembers.stream().collect(Collectors.toList());

            // does this actually work?
            Collections.sort(existingMembersSorted, (o1, o2) -> o1.compareTo(o2)); 

            // Find the next free suffix 
            // Wir brauchen die Integer, die muessen sortiert werden
            int index = 0;
            
            int newSuffix = 0;
            
            for (int suffix : existingMembersSorted) {
                if (suffix != index) {
                    newSuffix = index;

                    break;
                }

                index++;
            }




            if (newSuffix > 255) {
                //return "-1";
            }

            // use MMember here
            final Integer[] newMember = new Integer[]{3}; 

            this.subnets.get(subnet).put(newSuffix, newMember);

            // return this.toIPAddress(subnet, newSuffix);
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

    private void toIPAddress(String subnet, int suffix) {
        logger.trace("Converting to IP address" + subnet + suffix);
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

    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 8892;
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
