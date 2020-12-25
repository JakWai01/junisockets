package space.nebulark.junisockets.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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

import space.nebulark.junisockets.errors.ClientClosed;
import space.nebulark.junisockets.errors.ClientDoesNotExist;
import space.nebulark.junisockets.errors.UnimplementedOperation;
import space.nebulark.junisockets.models.*;
import space.nebulark.junisockets.operations.*;

public class SignalingServer extends WebSocketServer {

    private static HashMap<String, WebSocket> clients = new HashMap<String, WebSocket>();
    private static HashMap<String, MAlias> aliases = new HashMap<String, MAlias>();
    private static boolean isOpen = false;

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
        isOpen = true;

        logger.debug("Opening signaling server");

        try {
            ping();
        } catch (InterruptedException e) {
            // handle Client not responding error
            e.printStackTrace();
        }

        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String id = "";

        for (int i = 0; i < clients.size(); i++) {
            String currentKey = (String) clients.keySet().toArray()[i];

            if (clients.get(currentKey) == conn) {
                id = currentKey;
            }
        }

        logger.debug("Registering goodbye" + id);

        if (clients.containsKey(id)) {

            clients.remove(id);

            for (int i = 0; i < aliases.size(); i++) {
                String currentKey = (String) aliases.keySet().toArray()[i];
                if (currentKey == id) {
                    String key = currentKey;
                    aliases.remove(key);
                    removeIPAddress(key);
                    removeTCPAddress(key);

                    for (int j = 0; j < clients.size(); j++) {
                        send(clients.get(key), new Alias(id, key, false));

                        logger.debug("Sent alias" + id + key);
                    }
                }
            }

            send(new Goodbye(id));
            logger.debug("Sent alias" + id);
        } else {
            throw new ClientDoesNotExist();
        }

        logger.debug("Client disconnected" + id);

        isOpen = false;
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

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

            Thread thread = new Thread(() -> {
                handleKnock((JSONObject) operation.get("data"), conn);
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.OFFER.getValue())) {

            logger.debug("Received offer");

            Thread thread = new Thread(() -> {
                handleOffer((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.ANSWER.getValue())) {

            logger.debug("Received answer");

            Thread thread = new Thread(() -> {
                handleAnswer((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.CANDIDATE.getValue())) {

            logger.debug("Received candidate");

            Thread thread = new Thread(() -> {
                handleCandidate((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.BIND.getValue())) {

            logger.debug("Received bind");

            Thread thread = new Thread(() -> {
                handleBind((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.ACCEPTING.getValue())) {

            logger.debug("Received accepting");

            Thread thread = new Thread(() -> {
                handleAccepting((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.SHUTDOWN.getValue())) {

            logger.debug("Received shutdown");

            Thread thread = new Thread(() -> {
                handleShutdown((JSONObject) operation.get("data"));
            });
            thread.start();
        } else if (operation.get("opcode").equals(ESignalingOperationCode.CONNECT.getValue())) {
            logger.debug("Received connect");

            Thread thread = new Thread(() -> {
                handleConnect((JSONObject) operation.get("data"));
            });
            thread.start();
        } else {
            throw new UnimplementedOperation(operation.get("opcode"));
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
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    private static void handleKnock(JSONObject data, WebSocket conn) {

        logger.trace("Handling knock");

        String subnet = (String) data.get("subnet");

        final String id = createIPAddress(subnet);

        if (id != "-1") {
            send(conn, new Acknowledgement(id, false));
        } else {
            send(conn, new Acknowledgement(id, true));
            logger.debug("Knock rejected " + "{" + id + ", reason: subnet overflow}");

            return;
        }

        for (int i = 0; i < clients.size(); i++) {
            String existingId = (String) clients.keySet().toArray()[i];
            WebSocket existingClient = clients.get(existingId);

            if (existingId != id) {

                Thread thread = new Thread(() -> {
                    send(existingClient, new Greeting(existingId, id));
                    logger.debug("Sent greeting" + existingId + id);
                });

                thread.start();

            }
        }

        clients.put(id, conn);

        logger.trace("Client connected" + id);
    }

    public static void handleOffer(JSONObject data) {
        logger.trace("Handling offer: " + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            send(client, new Offer((String) data.get("offererId"), (String) data.get("answererId"),
                    (String) data.get("offer")));
            logger.debug("Sent offer" + data.get("offererId") + data.get("answererId") + data.get("offer"));
        });

        thread.start();

    }

    private static void handleAnswer(JSONObject data) {
        logger.trace("Handling answer: " + data);

        final WebSocket client = clients.get(data.get("offererId"));

        Thread thread = new Thread(() -> {
            send(client, new Answer((String) data.get("offererId"), (String) data.get("answererId"),
                    (String) data.get("answer")));
            logger.debug("Send answer" + data);
        });

        thread.start();

    }

    private static void handleCandidate(JSONObject data) {
        logger.trace("Handling candidate" + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            send(client, new Candidate((String) data.get("offererId"), (String) data.get("answererId"),
                    (String) data.get("candidate")));
            logger.debug("Sent candidate" + data);
        });

        thread.start();

    }

    private static void handleBind(JSONObject data) {
        logger.trace("Handling bind" + data);

        if (aliases.containsKey(data.get("alias"))) {
            logger.debug("Rejecting bind, alias already taken" + data);

            final WebSocket client = clients.get(data.get("id"));

            Thread thread = new Thread(() -> {
                send(client, new Alias((String) data.get("id"), (String) data.get("alias"), false));
            });

            thread.start();

        } else {
            logger.debug("Accepting bind" + data);

            claimTCPAddress((String) data.get("alias"));

            aliases.put((String) data.get("alias"), new MAlias((String) data.get("id"), false));

            for (int i = 0; i < clients.size(); i++) {
                Object key = clients.keySet().toArray()[i];

                Thread thread = new Thread(() -> {
                    send(clients.get(key), new Alias((String) data.get("id"), (String) data.get("alias"), true));
                    logger.debug("Sent alias" + data);
                });

                thread.start();

            }
        }
    }

    private static void handleAccepting(JSONObject data) {
        logger.trace("Handling accepting");

        if (!aliases.containsKey(data.get("alias")) || aliases.get(data.get("alias")).getId() != data.get("id")) {
            logger.debug("Rejecting accepting, alias does not exist" + data);
        } else {
            logger.debug("Accepting accepting" + data);

            aliases.put((String) data.get("alias"), new MAlias((String) data.get("id"), true));
        }
    }

    private static void handleShutdown(JSONObject data) {
        logger.trace("Handling shutdown");

        if (aliases.containsKey(data.get("alias")) || aliases.get(data.get("alias")).getId() != data.get("id")) {
            aliases.remove(data.get("alias"));
            removeTCPAddress((String) data.get("alias"));
            removeIPAddress((String) data.get("alias"));

            logger.debug("Accepting shutdown" + data);

            for (int i = 0; i < clients.size(); i++) {
                Object key = clients.keySet().toArray()[i];

                Thread thread = new Thread(() -> {
                    send(clients.get(key), new Alias((String) data.get("id"), (String) data.get("alias"), false));
                    logger.debug("Sent alias" + data);
                });

                thread.start();
            }

        } else {
            logger.debug("Rejecting shutdown, alias not taken or incorrect client ID" + data);

            final WebSocket client = clients.get(data.get("id"));

            Thread thread = new Thread(() -> {
                send(client, new Alias((String) data.get("id"), (String) data.get("alias"), true));
            });

            thread.start();

        }
    }

    private static void handleConnect(JSONObject data) {
        logger.trace("Handling connect");

        final String clientAlias = createTCPAddress((String) data.get("id"));
        final WebSocket client = clients.get(data.get("id"));

        if (!aliases.containsKey(data.get("remoteAlias")) || aliases.get(data.get("remoteAlias")).getAccepting()) {
            logger.debug("Rejecting connect, remote alias does not exists" + data);

            removeTCPAddress(clientAlias);

            Thread thread = new Thread(() -> {
                send(client, new Alias((String) data.get("id"), (String) data.get("alias"), false,
                        (String) data.get("clientConnectionId")));
            });

            thread.start();

        } else {
            logger.debug("Accepting connect" + data);

            aliases.put(clientAlias, new MAlias((String) data.get("id"), false));

            final Alias clientAliasMessage = new Alias((String) data.get("id"), clientAlias, true,
                    (String) data.get("clientConnectionId"), true);

            Thread thread = new Thread(() -> {
                send(client, clientAliasMessage);
                logger.debug("Sent alias for connection to client" + data + clientAliasMessage);
            });

            thread.start();

            logger.debug("Sent alias for connection to client" + data + clientAliasMessage);

            final MAlias serverId = aliases.get(data.get("remoteAlias"));
            final WebSocket server = clients.get(serverId.getId());

            final Alias serverAliasMessage = new Alias((String) data.get("id"), clientAlias, true);

            Thread thread2 = new Thread(() -> {
                send(server, serverAliasMessage);
                logger.debug("Sent alias for connection to server" + data + serverAliasMessage);
            });

            thread2.start();

            final Accept serverAcceptMessage = new Accept((String) data.get("remoteAlias"), clientAlias);

            Thread thread3 = new Thread(() -> {
                send(server, serverAcceptMessage);
                logger.debug("Sent accept to server" + data + serverAcceptMessage);
            });

            thread3.start();

            final Alias serverALiasForClientsMessage = new Alias((String) serverId.getId(),
                    (String) data.get("remoteAlias"), true, (String) data.get("clientConnectionId"));

            Thread thread4 = new Thread(() -> {
                send(client, serverALiasForClientsMessage);
                logger.debug("Sent alias for server to client" + data + serverALiasForClientsMessage);
            });

            thread4.start();
        }
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

    private static String createTCPAddress(String ipAddress) {
        logger.trace("Creating TCP address" + ipAddress);

        // lock

        try {
            final String[] partsIPAddress = parseIPAddress(ipAddress);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {
                    int[] intArray = Arrays.stream(subnets.get(subnet).get(Integer.parseInt(suffix)))
                            .mapToInt(Integer::intValue).toArray();

                    Arrays.sort(intArray);

                    int newPort = 0;

                    for (int i = 0; i < intArray.length; i++) {
                        if (intArray[i] != i) {
                            newPort = i;
                        }
                    }

                    int[] copy = new int[intArray.length + 1];

                    for (int i = 0; i < intArray.length; i++) {
                        copy[i] = intArray[i];
                    }

                    copy[copy.length - 1] = newPort;

                    Integer[] arr = Arrays.stream(copy).boxed().toArray(Integer[]::new);

                    subnets.get(subnet).replace(Integer.parseInt(suffix), arr);

                    return toTCPAddress(toIPAddress(subnet, Integer.parseInt(suffix)), newPort);
                } else {
                    throw new SuffixDoesNotExistError();
                }
            } else {
                throw new SubnetDoesNotExistError();
            }
        } finally {
            // release()
        }

        return "";
    }

    private static void claimTCPAddress(String tcpAddress) {
        logger.trace("Claiming TCP address" + tcpAddress);

        // lock

        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            final String[] partsIPAddress = parseIPAddress(partsTCPAddress[0]);

            Integer[] arr = new Integer[0];

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (!(subnets.get(subnet).containsKey(Integer.parseInt(suffix)))) {
                    subnets.get(subnet).put(Integer.parseInt(suffix), arr);
                }

                int count = 0;
                for (int i = 0; i < subnets.get(subnet).get(Integer.parseInt(suffix)).length; i++) {
                    if (subnets.get(subnet).get(Integer.parseInt(suffix))[i] == Integer.parseInt(partsTCPAddress[1])) {
                        count++;
                    }
                }

                if (count == 0) {
                    Integer[] copy = new Integer[subnets.get(subnet).get(Integer.parseInt(suffix)).length + 1];

                    for (int j = 0; j < subnets.get(subnet).get(Integer.parseInt(suffix)).length; j++) {
                        copy[j] = subnets.get(subnet).get(Integer.parseInt(suffix))[j];
                    }

                    copy[copy.length - 1] = Integer.parseInt(partsTCPAddress[1]);

                    subnets.get(subnet).replace(Integer.parseInt(suffix), copy);
                } else {
                    throw new PortAlreadyAllocatedError();
                    logger.fatal("Port already allocated");
                }
            } else {
                throw new SubnetDoesNotExistError();
                logger.fatal("Subnet does not exist");
            }
        } finally {
            // release()
        }
    }

    private static void removeIPAddress(String ipAddress) {
        logger.trace("Removing IP address" + ipAddress);

        // lock
        try {
            final String[] partsIPAddress = parseIPAddress(ipAddress);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {
                    subnets.get(subnet).remove(Integer.parseInt(suffix)); // We ensure above
                }
            }
        } finally {
            // release
        }
    }

    private static void removeTCPAddress(String tcpAddress) {
        logger.trace("Removing TCP address" + tcpAddress);

        // lock
        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            final String[] partsIPAddress = parseIPAddress(partsTCPAddress[0]);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {

                    Integer[] copy = new Integer[subnets.get(subnet).get(Integer.parseInt(suffix)).length - 1];

                    int count = 0;

                    for (int j = 0; j < subnets.get(subnet).get(Integer.parseInt(suffix)).length; j++) {
                        if (subnets.get(subnet).get(Integer.parseInt(suffix))[j] != Integer.parseInt(suffix)) {
                            copy[count] = subnets.get(subnet).get(Integer.parseInt(suffix))[j];
                            count++;
                        } else {
                            continue;
                        }
                    }
                    subnets.get(subnet).replace(Integer.parseInt(suffix), copy);
                }
            }
        } finally {
            // release();
        }
    }

    private static String toIPAddress(String subnet, int suffix) {
        logger.trace("Converting to IP address" + subnet + suffix);

        String ipAddress = subnet + "." + suffix;

        return ipAddress;
    }

    private static String toTCPAddress(String ipAddress, int port) {
        logger.trace("Converting to TCP address" + ipAddress + port);

        String tcpAddress = ipAddress + ":" + port;

        return tcpAddress;
    }

    private static String[] parseIPAddress(String ipAddress) {
        logger.trace("Parsing IP address" + ipAddress);

        return ipAddress.split(Pattern.quote("."));
    }

    private static String[] parseTCPAddress(String tcpAddress) {
        logger.trace("Parsing TCP address" + tcpAddress);

        return tcpAddress.split(":");
    }

    private static void send(WebSocket conn, Acknowledgement operation) {
        logger.debug("Sending" + operation);

        if (conn != null) {

            JSONObject obj = new JSONObject();
            String jsonText;

            Map m1 = new LinkedHashMap();
            m1.put("id", (String) operation.getId());
            m1.put("rejected", operation.getRejected());

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            Thread thread = new Thread(() -> {
                conn.send(jsonText);
            });

            thread.start();

        } else {
            throw new ClientClosedError();
        }
    }

    private static void send(WebSocket conn, Offer operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {

            JSONObject obj = new JSONObject();
            String jsonText;

            Map m1 = new LinkedHashMap();
            m1.put("offererId", (String) operation.getOffererId());
            m1.put("answererId", operation.getAnswererId());
            m1.put("offer", operation.getOffer());

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            Thread thread = new Thread(() -> {
                conn.send(jsonText);
            });

            thread.start();

        } else {
            throw new ClientClosedError();
        }
    }

    public static void send(WebSocket conn, Answer operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {

            JSONObject obj = new JSONObject();
            String jsonText;

            Map m1 = new LinkedHashMap();
            m1.put("offererId", (String) operation.getOffererId());
            m1.put("answererId", operation.getAnswererId());
            m1.put("answer", operation.getAnswer());

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            Thread thread = new Thread(() -> {
                conn.send(jsonText);
            });

            thread.start();

        } else {
            throw new ClientClosedError();
        }
    }

    public static void send(WebSocket conn, Candidate operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {

            JSONObject obj = new JSONObject();
            String jsonText;

            Map m1 = new LinkedHashMap();
            m1.put("offererId", (String) operation.getOffererId());
            m1.put("answererId", operation.getAnswererId());
            m1.put("candidate", operation.getCandidate());

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            Thread thread = new Thread(() -> {
                conn.send(jsonText);
            });

            thread.start();

        } else {
            throw new ClientClosedError();
        }
    }

    public static void send(WebSocket conn, Alias operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {

            JSONObject obj = new JSONObject();
            String jsonText;

            Map m1 = new LinkedHashMap();
            m1.put("id", (String) operation.getId());
            m1.put("alias", operation.getAlias());
            m1.put("set", operation.getSet());

            if (operation.getClientConnectionId() != null) {
                m1.put("clientConnectionId", operation.getClientConnectionId());
            }

            if (operation.getIsConnectionAlias()) {
                m1.put("isConnectionAlias", operation.getIsConnectionAlias());
            }

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            Thread thread = new Thread(() -> {
                conn.send(jsonText);
            });

            thread.start();

        } else {
            throw new ClientClosedError();
        }
    }

    public static void send(WebSocket conn, Accept operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {

            JSONObject obj = new JSONObject();
            String jsonText;

            Map m1 = new LinkedHashMap();
            m1.put("boundAlias", (String) operation.getBoundAlias());
            m1.put("clientAlias", operation.getClientAlias());

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            Thread thread = new Thread(() -> {
                conn.send(jsonText);
            });

            thread.start();

        } else {
            throw new ClientClosedError();
        }
    }

    public static void send(WebSocket conn, Greeting operation) {

        logger.debug("Sending" + operation);

        if (conn != null) {

            JSONObject obj = new JSONObject();
            String jsonText;

            Map m1 = new LinkedHashMap();
            m1.put("offererId", (String) operation.getOffererId());
            m1.put("answererId", operation.getAnswererId());

            obj.put("data", m1);
            obj.put("opcode", operation.opcode.getValue());

            jsonText = obj.toString();

            Thread thread = new Thread(() -> {
                conn.send(jsonText);
            });

            thread.start();

        } else {
            throw new ClientClosedError();
        }
    }

    public void send(Goodbye operation) {

        logger.debug("Sending" + operation);

        System.out.println("Entered connection");
        JSONObject obj = new JSONObject();
        String jsonText;

        Map m1 = new JSONObject();
        m1.put("id", operation.getId());

        obj.put("data", m1);
        obj.put("opcode", operation.opcode.getValue());

        jsonText = obj.toString();

        Thread thread = new Thread(() -> {
            broadcast(jsonText);
        });

        thread.start();

    }

    public static void ping() throws InterruptedException {
        
        while (isOpen == true) {
            
            for (int i = 0; i < clients.size(); i++) {
                String key = (String)clients.keySet().toArray()[i];

                clients.get(key).sendPing();
            }

            Thread.sleep(30000);

        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 8892;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        SignalingServer s = new SignalingServer(port);
        s.start();
        System.out.println("SignalingServer started on port: " + s.getPort());

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
