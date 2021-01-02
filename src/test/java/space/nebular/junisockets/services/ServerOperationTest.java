package space.nebular.junisockets.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

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

import space.nebulark.junisockets.services.SignalingServer;
import space.nebulark.junisockets.services.SignalingServerBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ServerOperationTest {
        
    @Test
    public void testHandleKnock() throws URISyntaxException, InterruptedException, IOException {
        
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
                Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.0\",\"rejected\":false},\"opcode\":\"acknowledged\"}", message);
                close();
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
            
        };

        cc.run();

        s.stop();
        
        // test more than 255 knocks
    }

    @Test
    public void testHandleOffer() throws URISyntaxException, IOException, InterruptedException {
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();
        
        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();
        
        s.start();
        // server started
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
                // handle answer
                JSONParser parser = new JSONParser();
                Object jsonObj = null;

                try {
                    jsonObj = parser.parse(message);
                } catch (org.json.simple.parser.ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                JSONObject operation = (JSONObject) jsonObj;
                
                Assert.assertEquals("answer", operation.get("opcode"));
                close();
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                
                send("{\"data\":{\"offererId\":\"127.0.0.0\", \"answererId\": \"127.0.0.1\", \"offer\": \"o1\"},\"opcode\":\"offer\"}");
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
                //Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.0\",\"rejected\":false},\"opcode\":\"acknowledged\"}", message);
                
                send("{\"data\": {\"offererId\": \"127.0.0.1\", \"answererId\": \"127.0.0.0\", \"answer\": \"a1\"}, \"opcode\": \"answer\"}");
                close();
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
            }
            
        };
        
        s.clients.put("127.0.0.0", cc.getConnection());
        s.clients.put("127.0.0.1", cc2.getConnection());

        Thread thread = new Thread(() -> {
            cc2.run();
        });

        thread.start();
        
        cc.run();

        s.stop();
        
    }

    public void testHandleAnswer() {

    }

    public void testHandleCandidate() {

    }

    public void handleBind() {

    }

    public void handleAccepting() {

    }

    public void handleShutdown() {

    }

    public void handleConnect() {

    }


}
