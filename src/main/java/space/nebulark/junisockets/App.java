package space.nebulark.junisockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import space.nebulark.junisockets.services.SignalingServer;
import space.nebulark.junisockets.services.SignalingServerBuilder;

public class App {
  public static void main(String[] args) throws InterruptedException, IOException {
        PropertyConfigurator.configure("log4j.properties");

        int port = 8892;
        String host = "localhost";
        Logger logger = Logger.getLogger(SignalingServer.class);

        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }

        try {
            host = args[1];
        } catch (Exception ex) {
        }

        //SignalingServer s = new SignalingServer(Logger.getLogger(SignalingServer.class), new InetSocketAddress(host, port));

        SignalingServerBuilder builder = new SignalingServerBuilder();
        
        SignalingServer s = builder.setHost(host).setLogger(logger).setPort(port).build();
        
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
