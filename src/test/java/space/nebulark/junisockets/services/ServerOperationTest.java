package space.nebulark.junisockets.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import space.nebulark.junisockets.addresses.IPAddress;
import space.nebulark.junisockets.addresses.TCPAddress;
import space.nebulark.junisockets.operations.ESignalingOperationCode;

/**
 * @see space.nebulark.junisockets.services.ServerOperation
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerOperationTest {

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleKnock()
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testHandleKnock() throws URISyntaxException, InterruptedException, IOException {
        System.out.println("testHandleKnock");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.0\",\"rejected\":false},\"opcode\":\"acknowledged\"}",
                        message);
                Assert.assertEquals(true, s.clients.containsKey("127.0.0.0"));

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                close();
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleBind()
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testHandleBind() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("testHandleBind");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        ReentrantLock mutex = new ReentrantLock();
        IPAddress ip = new IPAddress(logger, mutex, s.subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, s.subnets, ip);

        String tcpAddress = "127.0.0.0:1234";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);

        ip.createIPAddress(subnet);

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;
                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:1234\"},\"opcode\":\"bind\"}");
                }
                if (operation.get("opcode").equals(ESignalingOperationCode.ALIAS.getValue())) {
                    Assert.assertEquals(
                            "{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:1234\",\"set\":true},\"opcode\":\"alias\"}",
                            message);
                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");

            }
        };

        Thread.sleep(300);
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleOffer()
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testHandleOffer() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("testHandleOffer");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        s.start();

        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {

                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    if (((JSONObject) operation.get("data")).get("id").equals("127.0.0.0")) {
                        send("{\"data\":{\"offererId\":\"127.0.0.0\", \"answererId\": \"127.0.0.1\", \"offer\": \"o1\"},\"opcode\":\"offer\"}");
                    } else {
                        send("{\"data\":{\"offererId\":\"127.0.0.1\", \"answererId\": \"127.0.0.0\", \"offer\": \"o1\"},\"opcode\":\"offer\"}");
                    }
                }
                ;
                if (operation.get("opcode").equals(ESignalingOperationCode.GOODBYE.getValue())) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    close();
                }
                ;

            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        WebSocketClient cc2 = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;
                if (operation.get("opcode").equals(ESignalingOperationCode.OFFER.getValue())) {
                    Assert.assertEquals(
                            "{\"data\":{\"offererId\":\"" + ((JSONObject) operation.get("data")).get("offererId")
                                    + "\",\"answererId\":\"" + ((JSONObject) operation.get("data")).get("answererId")
                                    + "\",\"offer\":\"o1\"},\"opcode\":\"offer\"}",
                            message);

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc2.connect();
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleAnswer()
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testHandleAnswer() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("testHandleAnswer");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        s.start();

        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {

                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    if (((JSONObject) operation.get("data")).get("id").equals("127.0.0.0")) {
                        send("{\"data\":{\"offererId\":\"127.0.0.1\", \"answererId\": \"127.0.0.0\", \"answer\": \"o1\"},\"opcode\":\"answer\"}");
                    } else {
                        send("{\"data\":{\"offererId\":\"127.0.0.0\", \"answererId\": \"127.0.0.1\", \"answer\": \"o1\"},\"opcode\":\"answer\"}");
                    }
                }
                if (operation.get("opcode").equals(ESignalingOperationCode.GOODBYE.getValue())) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ;
                    close();
                }

            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        WebSocketClient cc2 = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;
                if (operation.get("opcode").equals(ESignalingOperationCode.ANSWER.getValue())) {
                    Assert.assertEquals(
                            "{\"data\":{\"offererId\":\"" + ((JSONObject) operation.get("data")).get("offererId")
                                    + "\",\"answererId\":\"" + ((JSONObject) operation.get("data")).get("answererId")
                                    + "\",\"answer\":\"o1\"},\"opcode\":\"answer\"}",
                            message);
                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc2.connect();
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleCandidate()
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    @Test
    public void testHandleCandidate() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("testHandleCandidate");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        s.start();

        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {

                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    if (((JSONObject) operation.get("data")).get("id").equals("127.0.0.0")) {
                        send("{\"data\":{\"offererId\":\"127.0.0.0\",\"answererId\":\"127.0.0.1\",\"candidate\":\"o1\"},\"opcode\":\"candidate\"}");
                    } else {
                        send("{\"data\":{\"offererId\":\"127.0.0.1\",\"answererId\":\"127.0.0.0\",\"candidate\":\"o1\"},\"opcode\":\"candidate\"}");
                    }
                }
                if (operation.get("opcode").equals(ESignalingOperationCode.CANDIDATE.getValue())) {
                    if (((JSONObject) operation.get("data")).get("offererId").equals("127.0.0.0")) {
                        Assert.assertEquals(
                                "{\"data\":{\"offererId\":\"127.0.0.0\",\"answererId\":\"127.0.0.1\",\"candidate\":\"o1b\"},\"opcode\":\"candidate\"}",
                                message);
                    } else {
                        Assert.assertEquals(
                                "{\"data\":{\"offererId\":\"127.0.0.1\",\"answererId\":\"127.0.0.0\",\"candidate\":\"o1b\"},\"opcode\":\"candidate\"}",
                                message);
                    }

                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        WebSocketClient cc2 = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;
                if (operation.get("opcode").equals(ESignalingOperationCode.CANDIDATE.getValue())) {
                    if (((JSONObject) operation.get("data")).get("offererId").equals("127.0.0.0")) {
                        send("{\"data\":{\"offererId\":\"127.0.0.1\",\"answererId\":\"127.0.0.0\",\"candidate\":\"o1b\"},\"opcode\":\"candidate\"}");
                    } else {
                        send("{\"data\":{\"offererId\":\"127.0.0.0\",\"answererId\":\"127.0.0.1\",\"candidate\":\"o1b\"},\"opcode\":\"candidate\"}");
                    }
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.GOODBYE.getValue())) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    close();
                 }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc2.connect();
        cc.run();
        Thread.sleep(300);
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleShutdown()
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    @Test
    public void testHandleShutdown() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("testHandleShutdown");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        ReentrantLock mutex = new ReentrantLock();
        IPAddress ip = new IPAddress(logger, mutex, s.subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, s.subnets, ip);

        String tcpAddress = "127.0.0.0:0";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);

        ip.createIPAddress(subnet);

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\"},\"opcode\":\"bind\"}");
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.ALIAS.getValue())) {
                    if (((JSONObject) operation.get("data")).get("set").toString().equals("true")) {
                        send("{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\"},\"opcode\":\"shutdown\"}");
                    } else {
                        Assert.assertEquals(
                                "{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\",\"set\":false},\"opcode\":\"alias\"}",
                                message);
                        close();
                    }
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleShutdown()
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testHandleShutdownRejected() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("testHandleShutdownRejected");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\"},\"opcode\":\"shutdown\"}");
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.ALIAS.getValue())) {
                    Assert.assertEquals(
                            "{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\",\"set\":true},\"opcode\":\"alias\"}",
                            message);
                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleAccepting()
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    @Test
    public void testHandleAccepting() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("testHandleAccepting");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        ReentrantLock mutex = new ReentrantLock();
        IPAddress ip = new IPAddress(logger, mutex, s.subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, s.subnets, ip);

        String tcpAddress = "127.0.0.0:0";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);

        ip.createIPAddress(subnet);

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\"},\"opcode\":\"bind\"}");
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.ALIAS.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\"},\"opcode\":\"accepting\"}");
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Assert.assertEquals(true,
                            s.aliases.get((String) ((JSONObject) operation.get("data")).get("alias")).getAccepting());
                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleAccepting()
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testHandleAcceptingRejected() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("testHandleAcceptingRejected");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        ReentrantLock mutex = new ReentrantLock();
        IPAddress ip = new IPAddress(logger, mutex, s.subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, s.subnets, ip);

        String tcpAddress = "127.0.0.0:0";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);

        ip.createIPAddress(subnet);

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.0\",\"alias\":\"127.0.0.0:0\"},\"opcode\":\"accepting\"}");

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Assert.assertEquals(false, s.aliases.containsKey("127.0.0.0:0"));
                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc.run();
        s.stop();

    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleConnect()
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testHandleConnect() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("testHandleConnect");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        ReentrantLock mutex = new ReentrantLock();
        IPAddress ip = new IPAddress(logger, mutex, s.subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, s.subnets, ip);

        String tcpAddress = "127.0.0.1:1234";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);

        ip.createIPAddress(subnet);

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {

                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.2\",\"clientConnectionId\":\"co1\",\"remoteAlias\":\"127.0.0.1:1234\"},\"opcode\":\"connect\"}");
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.ALIAS.getValue())) {
                    if (((JSONObject) operation.get("data")).get("id").equals("127.0.0.1")) {
                        if (((JSONObject) operation.get("data")).get("set").toString().equals("true")) {
                            Assert.assertEquals(
                                    "{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\",\"set\":true,\"clientConnectionId\":\"co1\"},\"opcode\":\"alias\"}",
                                    message);

                        } else {
                            Assert.assertEquals(
                                    "{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\",\"set\":false},\"opcode\":\"alias\"}",
                                    message);
                        }
                    } else {
                        Assert.assertEquals(
                                "{\"data\":{\"id\":\"127.0.0.2\",\"alias\":\"127.0.0.2:0\",\"set\":true,\"clientConnectionId\":\"co1\",\"isConnectionAlias\":true},\"opcode\":\"alias\"}",
                                message);
                    }
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.GOODBYE.getValue())) {
                    close();
                }

            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        WebSocketClient cc2 = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {

                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\"},\"opcode\":\"bind\"}");
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.ALIAS.getValue())) {

                    if (((JSONObject) operation.get("data")).get("id").equals("127.0.0.1")) {
                        send("{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\"},\"opcode\":\"accepting\"}");
                    } else {
                        Assert.assertEquals(
                                "{\"data\":{\"id\":\"127.0.0.2\",\"alias\":\"127.0.0.2:0\",\"set\":true},\"opcode\":\"alias\"}",
                                message);
                    }
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.ACCEPT.getValue())) {
                    Assert.assertEquals(
                            "{\"data\":{\"boundAlias\":\"127.0.0.1:1234\",\"clientAlias\":\"127.0.0.2:0\"},\"opcode\":\"accept\"}",
                            message);
                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc2.connect();
        cc.run();
        s.stop();
    }

    
    /** 
     * @see space.nebulark.junisockets.services.ServerOperation#handleConnect()
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testHandleConnectRejected() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("testHandleConnectRejected");
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();

        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();

        ReentrantLock mutex = new ReentrantLock();
        IPAddress ip = new IPAddress(logger, mutex, s.subnets);
        TCPAddress tcp = new TCPAddress(logger, mutex, s.subnets, ip);

        String tcpAddress = "127.0.0.1:1234";

        String[] partsTCPAddress = tcp.parseTCPAddress(tcpAddress);
        String[] partsIPAddress = ip.parseIPAddress(partsTCPAddress[0]);

        String subnet = String.join(".", partsIPAddress[0], partsIPAddress[1], partsIPAddress[2]);

        ip.createIPAddress(subnet);

        s.start();
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {

                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.2\",\"clientConnectionId\":\"co1\",\"remoteAlias\":\"127.0.0.1:1234\"},\"opcode\":\"connect\"}");
                }

                if (operation.get("opcode").equals(ESignalingOperationCode.ALIAS.getValue())) {
                    Assert.assertEquals(
                            "{\"data\":{\"id\":\"127.0.0.2\",\"alias\":null,\"set\":false},\"opcode\":\"alias\"}",
                            message);
                    close();
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
               send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        WebSocketClient cc2 = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onMessage(String message) {
              
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;

                if (operation.get("opcode").equals(ESignalingOperationCode.ACKNOWLEDGED.getValue())) {
                    send("{\"data\":{\"id\":\"127.0.0.1\",\"alias\":\"127.0.0.1:1234\"},\"opcode\":\"bind\"}");
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
               send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
        };

        Thread.sleep(300);
        cc2.connect();
        cc.run();
        Thread.sleep(300);
        s.stop();
    }
}
