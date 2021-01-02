package space.nebular.junisockets.services;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import space.nebulark.junisockets.services.SignalingServer;
import space.nebulark.junisockets.services.SignalingServerBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ServerOperationTest {
    
    @Test 
    public void testHandleKnock() throws URISyntaxException, InterruptedException {
        PropertyConfigurator.configure("log4j.properties");
        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        SignalingServerBuilder builder = new SignalingServerBuilder();
        
        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();
        
        s.start();
        //s.run();
        //ServerOperationTest c = new ServerOperationTest();
         
        WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8892")) {

            @Override
            public void onError(Exception ex) {
                // TODO Auto-generated method stub
                ex.printStackTrace();
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                // TODO Auto-generated method stub
                //super.onClose(code, reason, remote);
                System.out.println("close");
            }

            @Override
            public void onMessage(String message) {
                // TODO Auto-generated method stub
                //super.onMessage(message);
                System.out.println(message);
                Assert.assertEquals("{\"data\":{\"id\":\"127.0.0.0\",\"rejected\":false},\"opcode\":\"acknowledged\"}", message);
                close();
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                // TODO Auto-generated method stub
                //super.onOpen(handshakedata);
                System.out.println("open");
                send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            }
            
           // cc.run();
        };

        cc.run();
        //cc.connect();

//        Thread thread = new Thread(() -> {
  //          cc.run();
    //    });

//        thread.start();
           // cc.run();
        
        System.out.println("test");
        
    
        //cc.connect();
        
        //cc.getSocket() maybe this equals conn
        // if (cc.isOpen()) {
        //     System.out.println("Opened now");
        //     cc.send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");
            
        // } else {
        //     System.out.println("Not Connected");
        // }

        // if (cc.isClosed()) {
        //     System.out.println("closed");
        // }

        // if (cc.isClosing()) {
        //     System.out.println("asd");
        // }

        //cc.send("Hello");
        //cc.close(); 
        
        //c.connect();
        //c.send("{\"data\":{\"subnet\":\"127.0.0\"},\"opcode\":\"knock\"}");      

        //ServerOperationTest spy = Mockito.spy(c);

        // is this necessary?
        //Mockito.doReturn("").when(spy).onMessage("{\"data\":{\"id\":\"127.0.0.1\",\"rejected\":false},\"opcode\":\"acknowledged\"}");
        //when(c.onMessage("as")).thenAnswer(i -> i.getArguments()[0]);
        // what does onmessage get as string
        
    }
}
