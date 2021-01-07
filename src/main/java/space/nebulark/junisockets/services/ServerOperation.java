package space.nebulark.junisockets.services;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;

import space.nebulark.junisockets.addresses.IPAddress;
import space.nebulark.junisockets.addresses.TCPAddress;
import space.nebulark.junisockets.errors.ClientClosed;
import space.nebulark.junisockets.errors.PortAlreadyAllocated;
import space.nebulark.junisockets.errors.SubnetDoesNotExist;
import space.nebulark.junisockets.errors.SuffixDoesNotExist;
import space.nebulark.junisockets.models.MAlias;
import space.nebulark.junisockets.operations.Accept;
import space.nebulark.junisockets.operations.Acknowledgement;
import space.nebulark.junisockets.operations.Alias;
import space.nebulark.junisockets.operations.Answer;
import space.nebulark.junisockets.operations.Candidate;
import space.nebulark.junisockets.operations.ESignalingOperationCode;
import space.nebulark.junisockets.operations.Goodbye;
import space.nebulark.junisockets.operations.Greeting;
import space.nebulark.junisockets.operations.IOperation;
import space.nebulark.junisockets.operations.Offer;
import space.nebulark.junisockets.operations.OperationFactory;

/**
 * ServerOperation
 */
public class ServerOperation {
    private ConcurrentHashMap<String, WebSocket> clients;
    private ConcurrentHashMap<String, MAlias> aliases;
    private IPAddress ip;
    private TCPAddress tcpAddress;
    private Logger logger;

    /**
     * Constructor ServerOperation
     * @param clients clients
     * @param aliases aliases
     * @param ip ip
     * @param tcpAddress tcpAddress
     * @param logger logger
     */
    protected ServerOperation(ConcurrentHashMap<String, WebSocket> clients, ConcurrentHashMap<String, MAlias> aliases, IPAddress ip, TCPAddress tcpAddress, Logger logger) {
        this.clients = clients;
        this.aliases = aliases;
        this.ip = ip;
        this.tcpAddress = tcpAddress;
        this.logger = logger;
    }

    
    /** 
     * Handle client knock
     * @param data data
     * @param conn conn
     */
    protected void handleKnock(JSONObject data, WebSocket conn) {

        logger.debug("Handling knock");

        String subnet = (String) data.get("subnet");

        final String id = ip.createIPAddress(subnet);

        // If Id == -1, we exceeded the maximum of 255 subnets
        if (id != "-1") {
            try {
                send(conn, (Acknowledgement) new OperationFactory(ESignalingOperationCode.ACKNOWLEDGED).setId(id).setRejected(false).getOperation());
            } catch (ClientClosed e) {
                logger.error(e);
            }
        } else {
            try {
                send(conn, (Acknowledgement) new OperationFactory(ESignalingOperationCode.ACKNOWLEDGED).setId(id).setRejected(false).getOperation());
            } catch (ClientClosed e) {
                logger.error(e);
            }
            logger.debug("Knock rejected " + "{" + id + ", reason: subnet overflow}");

            return;
        }

        // Send Greeting to all clients, except the new one 
        for (int i = 0; i < clients.size(); i++) {
            String existingId = (String) clients.keySet().toArray()[i];
            WebSocket existingClient = clients.get(existingId);

            logger.debug("Existingid " + existingId + " id " + id);
            if (existingId != id) {

                Thread thread = new Thread(() -> {
                    try {
                        send(existingClient, (Greeting) new OperationFactory(ESignalingOperationCode.GREETING).setOffererId(existingId).setAnswererId(id).getOperation());
                    } catch (ClientClosed e) {
                        logger.error(e);
                    }
                    logger.debug("Sent greeting " + existingId + " " + id);
                });

                thread.start();

            }
        }

        clients.put(id, conn);

        logger.debug("Client connected " + id);
    }

    
    /** 
     * Handle client offer
     * @param data data
     */
    protected void handleOffer(JSONObject data) {
        logger.debug("Handling offer: " + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, (Offer) new OperationFactory(ESignalingOperationCode.OFFER).setOffererId((String) data.get("offererId")).setAnswererId((String) data.get("answererId")).setOffer((String) data.get("offer")).getOperation());
            } catch (ClientClosed e) {
                logger.error(e);
            }
            logger.debug("Sent offer " + data.get("offererId") + " " + data.get("answererId") + " " + data.get("offer"));
        });

        thread.start();

    }

    
    /** 
     * Handle client answer
     * @param data data
     */
    protected void handleAnswer(JSONObject data) {
        logger.debug("Handling answer: " + data);

        final WebSocket client = clients.get(data.get("offererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, (Answer) new OperationFactory(ESignalingOperationCode.ANSWER).setOffererId((String) data.get("offererId")).setAnswererId((String) data.get("answererId")).setAnswer((String) data.get("answer")).getOperation());       
            } catch (ClientClosed e) {
                logger.error(e);
            }
            logger.debug("Send answer " + data);
        });

        thread.start();

    }

    
    /** 
     * Handle client candidate
     * @param data data
     */
    protected void handleCandidate(JSONObject data) {
        logger.debug("Handling candidate " + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, (Candidate) new OperationFactory(ESignalingOperationCode.CANDIDATE).setOffererId((String) data.get("offererId")).setAnswererId((String) data.get("answererId")).setCandidate((String) data.get("candidate")).getOperation());
            } catch (ClientClosed e) {
                logger.error(e);
            }
            logger.debug("Sent candidate " + data);
        });

        thread.start();

    }

    
    /** 
     * Handle client bind
     * @param data data
     * @throws PortAlreadyAllocated Thrown if port is already allocated
     * @throws SubnetDoesNotExist Thrown if 
     */
    protected void handleBind(JSONObject data) throws PortAlreadyAllocated, SubnetDoesNotExist {
        logger.debug("Handling bind " + data);

        // Check if alias is alreay taken
        if (aliases.containsKey(data.get("alias"))) {
            logger.debug("Rejecting bind, alias already taken " + data);

            final WebSocket client = clients.get(data.get("id"));

            Thread thread = new Thread(() -> {
                try {
                   send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(false).getOperation());
                } catch (ClientClosed e) {
                    logger.error(e);
                }
            });

            thread.start();

        } else {
            logger.debug("Accepting bind " + data);

            tcpAddress.claimTCPAddress((String) data.get("alias"));

            // Add alias to aliases
            aliases.put((String) data.get("alias"), new MAlias((String) data.get("id"), false));

            // Send alias to all clients
            clients.forEach((id, client) -> {
                Thread thread = new Thread(() -> {
                    try {
                        send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(true).getOperation());
                        logger.debug("Sent alias " + data);
                    } catch (ClientClosed e) {
                        logger.error(e);
                    }
                });

                thread.start();
            });
        }
    }

    
    /** 
     * Handle client accepting
     * @param data data
     */
    protected void handleAccepting(JSONObject data) {
        logger.debug("Handling accepting");
        
        if (!aliases.containsKey(data.get("alias"))
                || !aliases.get(data.get("alias")).getId().equals((String) data.get("id"))) {
            logger.debug("Rejecting accepting, alias does not exist " + data);
        } else {
            logger.debug("Accepting accepting " + data);

            aliases.put((String) data.get("alias"), new MAlias((String) data.get("id"), true));
        }
    }

    
    /** 
     * Handle client shutdown
     * @param data data
     */
    protected void handleShutdown(JSONObject data) {
        logger.debug("Handling shutdown");

        // If aliases contains alias, remove alias, TCP address and IP address
        if (aliases.containsKey(data.get("alias")) && aliases.get(data.get("alias")).getId() != data.get("id")) {
            aliases.remove(data.get("alias"));
            tcpAddress.removeTCPAddress((String) data.get("alias"));
            ip.removeIPAddress(tcpAddress.parseTCPAddress((String) data.get("alias"))[0]);

            logger.debug("Accepting shutdown " + data);

            // Send alias to all clients
            clients.forEach((id, client) -> {
                Thread thread = new Thread(() -> {
                    try {
                        send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(false).getOperation());
                    } catch (ClientClosed e) {
                        logger.error(e);
                    }
                    logger.debug("Sent alias " + id + " " + data);
                });

                thread.start();
            });

        } else {
            logger.debug("Rejecting shutdown, alias not taken or incorrect client ID " + data);

            final WebSocket client = clients.get(data.get("id"));

            Thread thread = new Thread(() -> {
                try {
                    send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(true).getOperation());
                } catch (ClientClosed e) {
                    logger.error(e);
                }
            });

            thread.start();

        }
    }

    
    /** 
     * Handle client connect
     * @param data data
     * @throws SuffixDoesNotExist Thrown if suffix does not exist
     * @throws SubnetDoesNotExist Thrown if subent does not exist
     */
    protected void handleConnect(JSONObject data) throws SuffixDoesNotExist, SubnetDoesNotExist {
        logger.debug("Handling connect");

        final String clientAlias = tcpAddress.createTCPAddress((String) data.get("id"));
        final WebSocket client = clients.get(data.get("id"));

        // If aliases does not contain alias, reject connect
        if (!aliases.containsKey(data.get("remoteAlias")) || !aliases.get(data.get("remoteAlias")).getAccepting()) {
            logger.debug("Rejecting connect, remote alias does not exists " + data);
            tcpAddress.removeTCPAddress(clientAlias);

            final Alias aliasMessage = (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(false).setClientConnectionId((String) data.get("clientConnectionId")).getOperation();

            Thread thread = new Thread(() -> {
                try {                
                    send(client, aliasMessage);
                } catch (ClientClosed e) {
                    logger.error(e);
                }
                logger.debug("Sent alias to client " + data + " " + aliasMessage);
            });

            thread.start();

        } else {
            logger.debug("Accepting connect " + data);

            // Add clientAlias to aliases
            aliases.put(clientAlias, new MAlias((String) data.get("id"), false));

            final Alias clientAliasMessage = (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias(clientAlias).setSet(true).setClientConnectionId((String) data.get("clientConnectionId")).setIsConnectionAlias(true).getOperation();
            Thread thread = new Thread(() -> {
                try {
                    send(client, clientAliasMessage);
                } catch (ClientClosed e) {
                    logger.error(e);
                }
                logger.debug("Sent alias for connection to client " + data + " " + clientAliasMessage.getAsJSON(clientAliasMessage));
            });

            thread.start();

            final MAlias serverId = aliases.get(data.get("remoteAlias"));
            final WebSocket server = clients.get(serverId.getId());

            final Alias serverAliasMessage = (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias(clientAlias).setSet(true).getOperation();

            Thread thread2 = new Thread(() -> {
                try {
                    send(server, serverAliasMessage);
                } catch (ClientClosed e) {
                    logger.error(e);
                }
                logger.debug("Sent alias for connection to server " + data + " " + serverAliasMessage);
            });

            thread2.start();

            final Accept serverAcceptMessage = (Accept) new OperationFactory(ESignalingOperationCode.ACCEPT).setBoundAlias((String) data.get("remoteAlias")).setClientAlias(clientAlias).getOperation();
            Thread thread3 = new Thread(() -> {
                try {
                    send(server, serverAcceptMessage);
                } catch (ClientClosed e) {
                    logger.error(e);
                }
                logger.debug("Sent accept to server " + data + " " + serverAcceptMessage);
            });

            thread3.start();

            final Alias serverAliasForClientsMessage = (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) serverId.getId()).setAlias((String) data.get("remoteAlias")).setSet(true).setClientConnectionId((String) data.get("clientConnectionId")).getOperation();
            Thread thread4 = new Thread(() -> {
                try {
                    send(client, serverAliasForClientsMessage);
                } catch (ClientClosed e) {
                    logger.error(e);
                }
                logger.debug("Sent alias for server to client " + data + " " + serverAliasForClientsMessage);
            });

            thread4.start();
        }
    }

    
    /** 
     * Send goodbye from leaving client to all
     * @param operation operation
     */
    protected void send(Goodbye operation) {

        logger.debug("Sending " + operation);

        Thread thread = new Thread(() -> {
            // Broadcast Goodbye
            clients.forEach( (id, conn) -> {
                conn.send(operation.getAsJSON(operation));
            });

            logger.debug("Goodbye was send!");
        });

        thread.start();

    }

    
    /** 
     * Send response to client. E depends on which handler is sending. E might be one of the operations implementing the IOperation interface
     * @param <E> generic parameter
     * @param conn conn
     * @param operation operation
     * @throws ClientClosed Thrown if client is closed
     */
    protected <E extends IOperation> void send(WebSocket conn, E operation) throws ClientClosed {

        logger.debug("Sending " + operation.getOpCode());

        if (conn != null) {

            Thread thread = new Thread(() -> {
                // Send operations to given connection
                conn.send(operation.getAsJSON(operation));
            });

            thread.start();

        } else {
            throw new ClientClosed();
        }
    }
}
