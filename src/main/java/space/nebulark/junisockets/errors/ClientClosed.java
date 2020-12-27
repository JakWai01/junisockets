package space.nebulark.junisockets.errors;

public class ClientClosed extends Exception {
    
    /**
     *
     */
    private static final long serialVersionUID = -7585215220730792267L;

    public ClientClosed() {
        super("client is closed");
    }

}