package space.nebulark.junisockets.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
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
import space.nebulark.junisockets.errors.PortAlreadyAllocatedError;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;
import space.nebulark.junisockets.errors.UnimplementedOperation;
import space.nebulark.junisockets.models.*;
import space.nebulark.junisockets.operations.*;

public class SignalingServer extends WebSocketServer implements ISignalingService {

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

        Thread thread = new Thread(() -> {
            try {
                ping();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();

        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        //String id = "";

        // use streams instead
        // for (int i = 0; i < clients.size(); i++) {
        //     String currentKey = (String) clients.keySet().toArray()[i];

        //     if (clients.get(currentKey) == conn) {
        //         id = currentKey;
        //     }
        // }

        String id = clients.entrySet().stream().filter( e -> e.getValue() == conn).findFirst().map(e -> e.getKey()).toString();

        logger.debug("Registering goodbye" + id);

        if (clients.containsKey(id)) {

            clients.remove(id);

            // use streams instead
            // for (int i = 0; i < aliases.size(); i++) {
            //     String currentKey = (String) aliases.keySet().toArray()[i];
            //     if (currentKey == id) {
            //         String key = currentKey;
            //         aliases.remove(key);
            //         removeIPAddress(key);
            //         removeTCPAddress(key);

            //         // use streams instead
            //         for (int j = 0; j < clients.size(); j++) {
            //             try {
            //                 send(clients.get(key), new Alias(id, key, false));
            //             } catch (ClientClosed e) {
            //                 e.printStackTrace();
            //             }

            //             logger.debug("Sent alias" + id + key);
            //         }
            //     }
            // }
            
            // Example: clients.forEach((k,v) -> System.out.println("key: " + k + " value: " + v));

            aliases.forEach((clientId, alias) -> {
                if (clientId == id) {
                    // Unisockets removes with alias below
                    aliases.remove(clientId);
                    removeIPAddress(clientId);
                    removeTCPAddress(clientId);

                    clients.forEach( (key, client) -> {
                        // Das vielleicht noch in einene Thread packen
                        try {
                            send(clients.get(key), new Alias(id, clientId, false));
                        } catch (ClientClosed e1) {
                            e1.printStackTrace();
                        }

                        logger.debug("Sent alias" + id + key);
                    });

                }
            });

            // We broadcast Goodbye so we don't need to iterate over each client like in unisockets
            send(new Goodbye(id));

            logger.debug("Client disconnected" + id);
            
        } else {
                try {
                throw new ClientDoesNotExist();
            } catch (ClientDoesNotExist e1) {
                e1.printStackTrace();
            }
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

        try {
            handleOperation(operation, conn);
        } catch (UnimplementedOperation e) {
            e.printStackTrace();
        }

    }

    private static void handleOperation(JSONObject operation, WebSocket conn) throws UnimplementedOperation {

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
                try {
                    handleBind((JSONObject) operation.get("data"));
                } catch (PortAlreadyAllocatedError e) {
                    e.printStackTrace();
                } catch (SubnetDoesNotExist e) {
                    e.printStackTrace();
                }
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
                try {
                    handleConnect((JSONObject) operation.get("data"));
                } catch (SuffixDoesNotExist e) {
                    e.printStackTrace();
                } catch (SubnetDoesNotExist e) {
                    e.printStackTrace();
                }
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
            // careful, this behaves different
            ex.printStackTrace();
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
            try {
                send(conn, new Acknowledgement(id, false));
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
        } else {
            try {
                send(conn, new Acknowledgement(id, true));
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Knock rejected " + "{" + id + ", reason: subnet overflow}");

            return;
        }

        // use streams instead
        // for (int i = 0; i < clients.size(); i++) {
        //     String existingId = (String) clients.keySet().toArray()[i];
        //     WebSocket existingClient = clients.get(existingId);

        //     if (existingId != id) {

        //         Thread thread = new Thread(() -> {
        //             try {
        //                 send(existingClient, new Greeting(existingId, id));
        //             } catch (ClientClosed e) {
        //                 e.printStackTrace();
        //             }
        //             logger.debug("Sent greeting" + existingId + id);
        //         });

        //         thread.start();

        //     }
        // }

        clients.forEach( (existingId, existingClient) -> {
            if (existingId != id) {
                try {
                    send(existingClient, new Greeting(existingId, id));
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
            }

            logger.debug("Sent greeting " + existingId + id);
        });



        clients.put(id, conn);

        logger.trace("Client connected" + id);
    }

    public static void handleOffer(JSONObject data) {
        logger.trace("Handling offer: " + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, new Offer((String) data.get("offererId"), (String) data.get("answererId"),
                        (String) data.get("offer")));
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Sent offer" + data.get("offererId") + data.get("answererId") + data.get("offer"));
        });

        thread.start();

    }

    private static void handleAnswer(JSONObject data) {
        logger.trace("Handling answer: " + data);

        final WebSocket client = clients.get(data.get("offererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, new Answer((String) data.get("offererId"), (String) data.get("answererId"),
                        (String) data.get("answer")));
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Send answer" + data);
        });

        thread.start();

    }

    private static void handleCandidate(JSONObject data) {
        logger.trace("Handling candidate" + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, new Candidate((String) data.get("offererId"), (String) data.get("answererId"),
                        (String) data.get("candidate")));
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Sent candidate" + data);
        });

        thread.start();

    }

    private static void handleBind(JSONObject data) throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        logger.trace("Handling bind" + data);

        if (aliases.containsKey(data.get("alias"))) {
            logger.debug("Rejecting bind, alias already taken" + data);

            final WebSocket client = clients.get(data.get("id"));

            Thread thread = new Thread(() -> {
                try {
                    send(client, new Alias((String) data.get("id"), (String) data.get("alias"), false));
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
            });

            thread.start();

        } else {
            logger.debug("Accepting bind" + data);

            claimTCPAddress((String) data.get("alias"));

            aliases.put((String) data.get("alias"), new MAlias((String) data.get("id"), false));

            // use streams instead
            // for (int i = 0; i < clients.size(); i++) {
            //     Object key = clients.keySet().toArray()[i];

            //     Thread thread = new Thread(() -> {
            //         try {
            //             send(clients.get(key), new Alias((String) data.get("id"), (String) data.get("alias"), true));
            //         } catch (ClientClosed e) {
            //             e.printStackTrace();
            //         }
            //         logger.debug("Sent alias" + data);
            //     });

            //     thread.start();

            // }

            clients.forEach( (client, id) -> {
                Thread thread = new Thread(() -> {
                    try {
                        send(id, new Alias((String) data.get("id"), (String) data.get("alias"), true));
                    } catch (ClientClosed e) {
                        e.printStackTrace();
                    }
                });

                thread.start();
                
                logger.debug("Send alias" + data);
            });
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

            // use streams instead
            // for (int i = 0; i < clients.size(); i++) {
            //     Object key = clients.keySet().toArray()[i];

            //     Thread thread = new Thread(() -> {
            //         try {
            //             send(clients.get(key), new Alias((String) data.get("id"), (String) data.get("alias"), false));
            //         } catch (ClientClosed e) {
            //             e.printStackTrace();
            //         }
            //        logger.debug("Sent alias" + data);
            //     });

            //     thread.start();
            // }

            clients.forEach( (client, id) -> {
                Thread thread = new Thread(() -> {
                    try {
                        send(id, new Alias((String) data.get("id"), (String) data.get("alias"), false));
                    } catch (ClientClosed e) {
                        e.printStackTrace();
                    }
                });

                thread.start();

                logger.debug("Send alias" + id + data);
            });

        } else {
            logger.debug("Rejecting shutdown, alias not taken or incorrect client ID" + data);

            final WebSocket client = clients.get(data.get("id"));

            Thread thread = new Thread(() -> {
                try {
                    send(client, new Alias((String) data.get("id"), (String) data.get("alias"), true));
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
            });

            thread.start();

        }
    }

    private static void handleConnect(JSONObject data) throws SuffixDoesNotExist, SubnetDoesNotExist {
        logger.trace("Handling connect");

        final String clientAlias = createTCPAddress((String) data.get("id"));
        final WebSocket client = clients.get(data.get("id"));

        if (!aliases.containsKey(data.get("remoteAlias")) || aliases.get(data.get("remoteAlias")).getAccepting()) {
            logger.debug("Rejecting connect, remote alias does not exists" + data);

            removeTCPAddress(clientAlias);

            Thread thread = new Thread(() -> {
                try {
                    send(client, new Alias((String) data.get("id"), (String) data.get("alias"), false,
                            (String) data.get("clientConnectionId")));
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
            });

            thread.start();

        } else {
            logger.debug("Accepting connect" + data);

            aliases.put(clientAlias, new MAlias((String) data.get("id"), false));

            final Alias clientAliasMessage = new Alias((String) data.get("id"), clientAlias, true,
                    (String) data.get("clientConnectionId"), true);

            Thread thread = new Thread(() -> {
                try {
                    send(client, clientAliasMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent alias for connection to client" + data + clientAliasMessage);
            });

            thread.start();

            logger.debug("Sent alias for connection to client" + data + clientAliasMessage);

            final MAlias serverId = aliases.get(data.get("remoteAlias"));
            final WebSocket server = clients.get(serverId.getId());

            final Alias serverAliasMessage = new Alias((String) data.get("id"), clientAlias, true);

            Thread thread2 = new Thread(() -> {
                try {
                    send(server, serverAliasMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent alias for connection to server" + data + serverAliasMessage);
            });

            thread2.start();

            final Accept serverAcceptMessage = new Accept((String) data.get("remoteAlias"), clientAlias);

            Thread thread3 = new Thread(() -> {
                try {
                    send(server, serverAcceptMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent accept to server" + data + serverAcceptMessage);
            });

            thread3.start();

            final Alias serverALiasForClientsMessage = new Alias((String) serverId.getId(),
                    (String) data.get("remoteAlias"), true, (String) data.get("clientConnectionId"));

            Thread thread4 = new Thread(() -> {
                try {
                    send(client, serverALiasForClientsMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent alias for server to client" + data + serverALiasForClientsMessage);
            });

            thread4.start();
        }
    }

    //private static HashMap<String, HashMap<Integer, Integer[]>> subnets = new HashMap<String, HashMap<Integer, Integer[]>>();
    private static HashMap<String, HashMap<Integer, List<Integer>>> subnets = new HashMap<String, HashMap<Integer, List<Integer>>>();
    

    private static ReentrantLock mutex = new ReentrantLock();

    private static String createIPAddress(String subnet) {
        logger.trace("Creating IP address" + subnet);

        mutex.lock();

        try {
            // if (!subnets.containsKey(subnet)) {
            //     subnets.put(subnet, new HashMap<Integer, Integer[]>());
            // }
            if (!subnets.containsKey(subnet)) {
                    subnets.put(subnet, new HashMap<Integer, List<Integer>>());
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

            // Integer[] newMember = new Integer[0];
            List<Integer> newMember = new ArrayList<Integer>();

            subnets.get(subnet).put(newSuffix, newMember); // We ensure above

            return toIPAddress(subnet, newSuffix);

        } finally {
            mutex.unlock();
        }
    }

    private static String createTCPAddress(String ipAddress) throws SuffixDoesNotExist, SubnetDoesNotExist {
        logger.trace("Creating TCP address" + ipAddress);

        mutex.lock();

        try {
            final String[] partsIPAddress = parseIPAddress(ipAddress);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {


                    // int[] intArray = Arrays.stream(subnets.get(subnet).get(Integer.parseInt(suffix)))
                    //         .mapToInt(Integer::intValue).toArray();

                    // Arrays.sort(intArray);

                    subnets.get(subnet).get(Integer.parseInt(suffix)).sort((a,b) -> a - b);

                    int newPort = 0;

                    // use streams instead and a collection instead of an array above
                    // for (int i = 0; i < intArray.length; i++) {
                    //     if (intArray[i] != i) {
                    //         newPort = i;
                    //     }
                    // }

                    for (int i = 0; i < subnets.get(subnet).get(Integer.parseInt(suffix)).size(); i++) {
                        if (subnets.get(subnet).get(Integer.parseInt(suffix)).get(i) != i) {
                            newPort = i;
                        }
                    }

                    // int[] copy = new int[intArray.length + 1];

                    // // use streams instead so we do not need to perform this step
                    // for (int i = 0; i < intArray.length; i++) {
                    //     copy[i] = intArray[i];
                    // }

                    // copy[copy.length - 1] = newPort;

                    // Integer[] arr = Arrays.stream(copy).boxed().toArray(Integer[]::new);

                    // subnets.get(subnet).replace(Integer.parseInt(suffix), arr);

                    subnets.get(subnet).get(Integer.parseInt(suffix)).add(newPort);

                    return toTCPAddress(toIPAddress(subnet, Integer.parseInt(suffix)), newPort);
                } else {
                    throw new SuffixDoesNotExist();
                }
            } else {
                throw new SubnetDoesNotExist();
            }
        } finally {
            mutex.unlock();
        }
    }

    private static void claimTCPAddress(String tcpAddress) throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        logger.trace("Claiming TCP address" + tcpAddress);

        mutex.lock();

        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            final String[] partsIPAddress = parseIPAddress(partsTCPAddress[0]);

            // Integer[] arr = new Integer[0];
            List<Integer> member = new ArrayList<Integer>();

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (!(subnets.get(subnet).containsKey(Integer.parseInt(suffix)))) {
                    subnets.get(subnet).put(Integer.parseInt(suffix), member); // We ensure above
                }

                if (subnets.get(subnet).get(Integer.parseInt(suffix)).stream().filter(e -> e == Integer.parseInt(partsTCPAddress[1])).collect(Collectors.toList()).size() == 0) {
                    subnets.get(subnet).get(Integer.parseInt(suffix)).add(Integer.parseInt(partsTCPAddress[1]));
                }
                // int count = 0;

                // // use streams instead
                // for (int i = 0; i < subnets.get(subnet).get(Integer.parseInt(suffix)).length; i++) {
                //     if (subnets.get(subnet).get(Integer.parseInt(suffix))[i] == Integer.parseInt(partsTCPAddress[1])) {
                //         count++;
                //     }
                // }

                // if (count == 0) {
                //     Integer[] copy = new Integer[subnets.get(subnet).get(Integer.parseInt(suffix)).length + 1];

                //     // use streams instead so we can skip that step
                //     for (int j = 0; j < subnets.get(subnet).get(Integer.parseInt(suffix)).length; j++) {
                //         copy[j] = subnets.get(subnet).get(Integer.parseInt(suffix))[j];
                //     }

                //     copy[copy.length - 1] = Integer.parseInt(partsTCPAddress[1]);

                //     subnets.get(subnet).replace(Integer.parseInt(suffix), copy);
                 else {
                    throw new PortAlreadyAllocatedError();
                }
            } else {
                throw new SubnetDoesNotExist();
            }
        } finally {
            mutex.unlock();
        }
    }

    private static void removeIPAddress(String ipAddress) {
        logger.trace("Removing IP address" + ipAddress);

        mutex.lock();

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
            mutex.unlock();
        }
    }

    private static void removeTCPAddress(String tcpAddress) {
        logger.trace("Removing TCP address" + tcpAddress);

        mutex.lock();

        try {
            final String[] partsTCPAddress = parseTCPAddress(tcpAddress);
            final String[] partsIPAddress = parseIPAddress(partsTCPAddress[0]);

            String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);
            String suffix = partsIPAddress[3];

            if (subnets.containsKey(subnet)) {
                if (subnets.get(subnet).containsKey(Integer.parseInt(suffix))) {

                    Integer[] copy = new Integer[subnets.get(subnet).get(Integer.parseInt(suffix)).length - 1];

                    int count = 0;

                    // use streams instead
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
            mutex.unlock();
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

    private static void send(WebSocket conn, Acknowledgement operation) throws ClientClosed {
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
            throw new ClientClosed();
        }
    }

    private static void send(WebSocket conn, Offer operation) throws ClientClosed {

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
            throw new ClientClosed();
        }
    }

    public static void send(WebSocket conn, Answer operation) throws ClientClosed {

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
            throw new ClientClosed();
        }
    }

    public static void send(WebSocket conn, Candidate operation) throws ClientClosed {

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
            throw new ClientClosed();
        }
    }

    public static void send(WebSocket conn, Alias operation) throws ClientClosed {

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
            throw new ClientClosed();
        }
    }

    public static void send(WebSocket conn, Accept operation) throws ClientClosed {

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
            throw new ClientClosed();
        }
    }

    public static void send(WebSocket conn, Greeting operation) throws ClientClosed {

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
            throw new ClientClosed();
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

            // use streams instead
            for (int i = 0; i < clients.size(); i++) {
                String key = (String) clients.keySet().toArray()[i];

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
