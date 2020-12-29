package space.nebulark.junisockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import org.apache.log4j.BasicConfigurator;

import space.nebulark.junisockets.services.SignalingServer;

public class App {
  public static void main(String[] args) throws InterruptedException, IOException {
        BasicConfigurator.configure();
        int port = 8892;
        String host = "localhost";

        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }

        try {
            host = args[1];
        } catch (Exception ex) {
        }

        SignalingServer s = new SignalingServer(new InetSocketAddress(host, port));
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
