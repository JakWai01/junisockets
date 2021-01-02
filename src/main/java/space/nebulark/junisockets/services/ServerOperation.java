package space.nebulark.junisockets.services;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;

import space.nebulark.junisockets.addresses.IPAddress;
import space.nebulark.junisockets.addresses.TCPAddress;
import space.nebulark.junisockets.errors.ClientClosed;
import space.nebulark.junisockets.errors.PortAlreadyAllocatedError;
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
import space.nebulark.junisockets.operations.IServerOperation;
import space.nebulark.junisockets.operations.Offer;
import space.nebulark.junisockets.operations.OperationFactory;

public class ServerOperation implements IServerOperation {
    ConcurrentHashMap<String, WebSocket> clients;
    ConcurrentHashMap<String, MAlias> aliases;
    IPAddress ip;
    TCPAddress tcpAddress;
    Logger logger;

    public ServerOperation(ConcurrentHashMap<String, WebSocket> clients, ConcurrentHashMap<String, MAlias> aliases, IPAddress ip, TCPAddress tcpAddress, Logger logger) {
        this.clients = clients;
        this.aliases = aliases;
        this.ip = ip;
        this.tcpAddress = tcpAddress;
        this.logger = logger;
    }

    public void handleKnock(JSONObject data, WebSocket conn) {

        logger.debug("Handling knock");

        String subnet = (String) data.get("subnet");

        final String id = ip.createIPAddress(subnet);

        if (id != "-1") {
            try {
                send(conn, (Acknowledgement) new OperationFactory(ESignalingOperationCode.ACKNOWLEDGED).setId(id).setRejected(false).getOperation());
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
        } else {
            try {
                send(conn, (Acknowledgement) new OperationFactory(ESignalingOperationCode.ACKNOWLEDGED).setId(id).setRejected(false).getOperation());
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Knock rejected " + "{" + id + ", reason: subnet overflow}");

            return;
        }

        for (int i = 0; i < clients.size(); i++) {
            String existingId = (String) clients.keySet().toArray()[i];
            WebSocket existingClient = clients.get(existingId);

            logger.debug("Existingid " + existingId + " id " + id);
            if (existingId != id) {

                Thread thread = new Thread(() -> {
                    try {
                        send(existingClient, (Greeting) new OperationFactory(ESignalingOperationCode.GREETING).setOffererId(existingId).setAnswererId(id).getOperation());
                    } catch (ClientClosed e) {
                        e.printStackTrace();
                    }
                    logger.debug("Sent greeting " + existingId + " " + id);
                });

                thread.start();

            }
        }

        clients.put(id, conn);

        logger.debug("Client connected " + id);
    }

    public void handleOffer(JSONObject data) {
        logger.debug("Handling offer: " + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, (Offer) new OperationFactory(ESignalingOperationCode.OFFER).setOffererId((String) data.get("offererId")).setAnswererId((String) data.get("answererId")).setOffer((String) data.get("offer")).getOperation());
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Sent offer " + data.get("offererId") + " " + data.get("answererId") + " " + data.get("offer"));
        });

        thread.start();

    }

    public void handleAnswer(JSONObject data) {
        logger.debug("Handling answer: " + data);

        final WebSocket client = clients.get(data.get("offererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, (Answer) new OperationFactory(ESignalingOperationCode.ANSWER).setOffererId((String) data.get("offererId")).setAnswererId((String) data.get("answererId")).setAnswer((String) data.get("answer")).getOperation());       
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Send answer " + data);
        });

        thread.start();

    }

    public void handleCandidate(JSONObject data) {
        logger.debug("Handling candidate " + data);

        final WebSocket client = clients.get(data.get("answererId"));

        Thread thread = new Thread(() -> {
            try {
                send(client, (Candidate) new OperationFactory(ESignalingOperationCode.CANDIDATE).setOffererId((String) data.get("offererId")).setAnswererId((String) data.get("answererId")).setCandidate((String) data.get("candidate")).getOperation());
            } catch (ClientClosed e) {
                e.printStackTrace();
            }
            logger.debug("Sent candidate " + data);
        });

        thread.start();

    }

    public void handleBind(JSONObject data) throws PortAlreadyAllocatedError, SubnetDoesNotExist {
        logger.debug("Handling bind " + data);

        if (aliases.containsKey(data.get("alias"))) {
            logger.debug("Rejecting bind, alias already taken " + data);

            final WebSocket client = clients.get(data.get("id"));

            Thread thread = new Thread(() -> {
                try {
                   send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(false).getOperation());
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
            });

            thread.start();

        } else {
            logger.debug("Accepting bind " + data);

            tcpAddress.claimTCPAddress((String) data.get("alias"));

            aliases.put((String) data.get("alias"), new MAlias((String) data.get("id"), false));

            clients.forEach((id, client) -> {
                //Thread thread = new Thread(() -> {
                    try {
                        send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(true).getOperation());
                    } catch (ClientClosed e) {
                        e.printStackTrace();
                        logger.debug("Sent alias " + data);
                    }
                //});

//                thread.start();
            });
        }
    }

    public void handleAccepting(JSONObject data) {
        logger.debug("Handling accepting");
        
        if (!aliases.containsKey(data.get("alias"))
                || !aliases.get(data.get("alias")).getId().equals((String) data.get("id"))) {
            logger.debug("Rejecting accepting, alias does not exist " + data);
        } else {
            logger.debug("Accepting accepting " + data);

            aliases.put((String) data.get("alias"), new MAlias((String) data.get("id"), true));
        }
    }

    public void handleShutdown(JSONObject data) {
        logger.debug("Handling shutdown");

        if (aliases.containsKey(data.get("alias")) && aliases.get(data.get("alias")).getId() != data.get("id")) {
            aliases.remove(data.get("alias"));
            tcpAddress.removeTCPAddress((String) data.get("alias"));
            ip.removeIPAddress((String) data.get("alias"));

            logger.debug("Accepting shutdown " + data);

            clients.forEach((id, client) -> {
                Thread thread = new Thread(() -> {
                    try {
                        send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(false).getOperation());
                    } catch (ClientClosed e) {
                        e.printStackTrace();
                    }
                    logger.debug("Send alias " + id + data);
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
                    e.printStackTrace();
                }
            });

            thread.start();

        }
    }

    public void handleConnect(JSONObject data) throws SuffixDoesNotExist, SubnetDoesNotExist {
        logger.debug("Handling connect");

        final String clientAlias = tcpAddress.createTCPAddress((String) data.get("id"));
        final WebSocket client = clients.get(data.get("id"));

        if (!aliases.containsKey(data.get("remoteAlias")) || !aliases.get(data.get("remoteAlias")).getAccepting()) {
            logger.debug("Rejecting connect, remote alias does not exists " + data);

            tcpAddress.removeTCPAddress(clientAlias);

            Thread thread = new Thread(() -> {
                try {                
                    send(client, (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias((String) data.get("alias")).setSet(false).setClientConnectionId((String) data.get("clientConnectionId")).getOperation());
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
            });

            thread.start();

        } else {
            logger.debug("Accepting connect " + data);

            aliases.put(clientAlias, new MAlias((String) data.get("id"), false));

            final Alias clientAliasMessage = (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias(clientAlias).setSet(true).setClientConnectionId((String) data.get("clientConnectionId")).setIsConnectionAlias(true).getOperation();
            Thread thread = new Thread(() -> {
                try {
                    send(client, clientAliasMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent alias for connection to client " + data + " " + clientAliasMessage);
            });

            thread.start();

            logger.debug("Sent alias for connection to client " + data + " " + clientAliasMessage);

            final MAlias serverId = aliases.get(data.get("remoteAlias"));
            final WebSocket server = clients.get(serverId.getId());

            final Alias serverAliasMessage = (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) data.get("id")).setAlias(clientAlias).setSet(true).getOperation();

            Thread thread2 = new Thread(() -> {
                try {
                    send(server, serverAliasMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent alias for connection to server " + data + " " + serverAliasMessage);
            });

            thread2.start();

            final Accept serverAcceptMessage = (Accept) new OperationFactory(ESignalingOperationCode.ACCEPT).setBoundAlias((String) data.get("remoteAlias")).setClientAlias(clientAlias).getOperation();
            Thread thread3 = new Thread(() -> {
                try {
                    send(server, serverAcceptMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent accept to server " + data + " " + serverAcceptMessage);
            });

            thread3.start();

            final Alias serverAliasForClientsMessage = (Alias) new OperationFactory(ESignalingOperationCode.ALIAS).setId((String) serverId.getId()).setAlias((String) data.get("remoteAlias")).setSet(true).setClientConnectionId((String) data.get("clientConnectionId")).getOperation();
            Thread thread4 = new Thread(() -> {
                try {
                    send(client, serverAliasForClientsMessage);
                } catch (ClientClosed e) {
                    e.printStackTrace();
                }
                logger.debug("Sent alias for server to client " + data + " " + serverAliasForClientsMessage);
            });

            thread4.start();
        }
    }

    public void send(Goodbye operation) {

        logger.debug("Sending " + operation);

        Thread thread = new Thread(() -> {
            clients.forEach( (id, conn) -> {
                conn.send(operation.getAsJSON(operation));
            });

            logger.debug("Goodbye was send!");
        });

        thread.start();

    }

    public <E extends IOperation> void send(WebSocket conn, E operation) throws ClientClosed {

        logger.debug("Sending " + operation.getOpCode());

        if (conn != null) {

            //Thread thread = new Thread(() -> {
                conn.send(operation.getAsJSON(operation));
            //});

            //thread.start();

        } else {
            throw new ClientClosed();
        }
    }
}
