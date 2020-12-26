package space.nebulark.junisockets.operations;

import org.java_websocket.WebSocket;

public interface ISignalingService {
   
    private static void send(WebSocket conn, Acknowledgement operation){};

    private static void send(WebSocket conn, Offer operation){};

    public static void send(WebSocket conn, Answer operation){};

    public static void send(WebSocket conn, Candidate operation){};

    public static void send(WebSocket conn, Alias operation){};

    public static void send(WebSocket conn, Accept operation){};
    
    public static void send(WebSocket conn, Greeting operation){};

    public void send(Goodbye operation);

}
