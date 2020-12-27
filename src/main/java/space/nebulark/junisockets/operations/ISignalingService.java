package space.nebulark.junisockets.operations;

import org.java_websocket.WebSocket;

public interface ISignalingService {
   
    static void send(WebSocket conn, Acknowledgement operation){};

    static void send(WebSocket conn, Offer operation){};

    static void send(WebSocket conn, Answer operation){};

    static void send(WebSocket conn, Candidate operation){};

    static void send(WebSocket conn, Alias operation){};

    static void send(WebSocket conn, Accept operation){};
    
    static void send(WebSocket conn, Greeting operation){};

    void send(Goodbye operation);

}
