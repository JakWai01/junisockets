package space.nebulark.junisockets.errors;

public class ClientClosed extends Exception {
    
    public ClientClosed() {
        super("client is closed");
    }

}